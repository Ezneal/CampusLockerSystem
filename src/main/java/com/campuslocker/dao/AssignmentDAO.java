
package com.campuslocker.dao;

import com.campuslocker.model.Locker;
import com.campuslocker.model.Student;
import com.campuslocker.model.OccupiedStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AssignmentDAO {

    // Assign an available locker to a student.
    public void assignLocker(Student student, Locker locker)
            throws SQLException {

        if (student == null || locker == null) {
            throw new SQLException("Student and locker are required.");
        }

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                // 1. Verify and lock the student record.
                try (PreparedStatement statement =
                        connection.prepareStatement(
                                "SELECT student_id FROM students "
                                + "WHERE student_id = ? FOR UPDATE")) {

                    statement.setString(1, student.getStudentID());

                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            throw new SQLException(
                                    "Student does not exist.");
                        }
                    }
                }

                // 2. Check whether the student already has a locker.
                try (PreparedStatement statement =
                        connection.prepareStatement(
                                "SELECT assignment_id FROM assignments "
                                + "WHERE student_id = ? LIMIT 1")) {

                    statement.setString(1, student.getStudentID());

                    try (ResultSet result = statement.executeQuery()) {
                        if (result.next()) {
                            throw new SQLException(
                                    "This student already has an active locker.");
                        }
                    }
                }

                // 3. Lock the locker and verify its status.
                try (PreparedStatement statement =
                        connection.prepareStatement(
                                "SELECT status FROM lockers "
                                + "WHERE locker_number = ? FOR UPDATE")) {

                    statement.setString(1, locker.getLockerNumber());

                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            throw new SQLException(
                                    "Locker does not exist.");
                        }

                        String status = result.getString("status");

                        if (!"Available".equals(status)) {
                            throw new SQLException(
                                    "Locker is not available. Current status: "
                                    + status);
                        }
                    }
                }

                // 4. Create the assignment.
                try (PreparedStatement statement =
                        connection.prepareStatement(
                                "INSERT INTO assignments "
                                + "(student_id, locker_number, assign_date) "
                                + "VALUES (?, ?, ?)")) {

                    statement.setString(1, student.getStudentID());
                    statement.setString(2, locker.getLockerNumber());
                    statement.setDate(
                            3,
                            java.sql.Date.valueOf(
                                    java.time.LocalDate.now()));

                    statement.executeUpdate();
                }

                // 5. Update the locker status.
                try (PreparedStatement statement =
                        connection.prepareStatement(
                                "UPDATE lockers SET status = 'Occupied' "
                                + "WHERE locker_number = ? "
                                + "AND status = 'Available'")) {

                    statement.setString(1, locker.getLockerNumber());

                    if (statement.executeUpdate() != 1) {
                        throw new SQLException(
                                "Locker availability changed. Please try again.");
                    }
                }

                connection.commit();
                locker.setStatus(new OccupiedStatus(student));

                System.out.println(
                        "Locker " + locker.getLockerNumber()
                        + " successfully assigned to "
                        + student.getName() + ".");

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    // Display the assignments currently recorded in the database.
    public List<String> getActiveAssignments() throws SQLException {

        List<String> assignments = new ArrayList<>();

        String sql =
                "SELECT a.assignment_id, s.student_id, s.name, "
                + "l.locker_number, a.assign_date "
                + "FROM assignments a "
                + "JOIN students s ON a.student_id = s.student_id "
                + "JOIN lockers l ON a.locker_number = l.locker_number "
                + "ORDER BY a.assignment_id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                assignments.add(
                        "Assignment #" + result.getInt("assignment_id")
                        + " | " + result.getString("student_id")
                        + " - " + result.getString("name")
                        + " | Locker: " + result.getString("locker_number")
                        + " | Date: " + result.getDate("assign_date"));
            }
        }

        return assignments;
    }

    // Release an occupied locker and assign it to the next eligible
    // student on the waitlist, if one exists.
    public void releaseLocker(String lockerNumber) throws SQLException {

        if (lockerNumber == null || lockerNumber.trim().isEmpty()) {
            throw new SQLException("Locker number is required.");
        }

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                // 1. Lock the locker and verify its current status.
                String status;

                try (PreparedStatement statement =
                        connection.prepareStatement(
                                "SELECT status FROM lockers "
                                + "WHERE locker_number = ? FOR UPDATE")) {

                    statement.setString(1, lockerNumber);

                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            throw new SQLException(
                                    "Locker does not exist.");
                        }

                        status = result.getString("status");
                    }
                }

                if (!"Occupied".equals(status)) {
                    throw new SQLException(
                            "Only occupied lockers can be released. "
                            + "Current status: " + status);
                }

                // 2. Find the current assignment.
                int assignmentID;

                try (PreparedStatement statement =
                        connection.prepareStatement(
                                "SELECT assignment_id FROM assignments "
                                + "WHERE locker_number = ? "
                                + "FOR UPDATE")) {

                    statement.setString(1, lockerNumber);

                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            throw new SQLException(
                                    "No assignment found for this locker.");
                        }

                        assignmentID = result.getInt("assignment_id");

                        if (result.next()) {
                            throw new SQLException(
                                    "Multiple assignments exist for this locker. "
                                    + "Please review the database records.");
                        }
                    }
                }

                // 3. Find the earliest waitlisted student who does not
                // already have an assignment.
                Integer queueID = null;
                String nextStudentID = null;

                String findNextSQL =
                        "SELECT w.queue_id, w.student_id "
                        + "FROM waitlist w "
                        + "WHERE NOT EXISTS ("
                        + " SELECT 1 FROM assignments a "
                        + " WHERE a.student_id = w.student_id"
                        + ") "
                        + "ORDER BY w.queue_id "
                        + "LIMIT 1 FOR UPDATE";

                try (PreparedStatement statement =
                        connection.prepareStatement(findNextSQL);
                     ResultSet result = statement.executeQuery()) {

                    if (result.next()) {
                        queueID = result.getInt("queue_id");
                        nextStudentID = result.getString("student_id");
                    }
                }

                // 4. Remove the old assignment only after validation.
                try (PreparedStatement statement =
                        connection.prepareStatement(
                                "DELETE FROM assignments "
                                + "WHERE assignment_id = ?")) {

                    statement.setInt(1, assignmentID);

                    if (statement.executeUpdate() != 1) {
                        throw new SQLException(
                                "Could not remove the current assignment.");
                    }
                }

                String newStatus = "Available";

                // 5. Give the locker to the next eligible student.
                if (nextStudentID != null) {

                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                    "INSERT INTO assignments "
                                    + "(student_id, locker_number, assign_date) "
                                    + "VALUES (?, ?, ?)")) {

                        statement.setString(1, nextStudentID);
                        statement.setString(2, lockerNumber);
                        statement.setDate(
                                3,
                                java.sql.Date.valueOf(
                                        java.time.LocalDate.now()));

                        statement.executeUpdate();
                    }

                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                    "DELETE FROM waitlist WHERE queue_id = ?")) {

                        statement.setInt(1, queueID);

                        if (statement.executeUpdate() != 1) {
                            throw new SQLException(
                                    "Could not remove the student from the waitlist.");
                        }
                    }

                    newStatus = "Occupied";

                    System.out.println(
                            "Locker " + lockerNumber
                            + " assigned to waiting student "
                            + nextStudentID + ".");

                } else {
                    System.out.println(
                            "Locker " + lockerNumber
                            + " is now available.");
                }

                // 6. Update the final locker status.
                try (PreparedStatement statement =
                        connection.prepareStatement(
                                "UPDATE lockers SET status = ? "
                                + "WHERE locker_number = ? "
                                + "AND status = 'Occupied'")) {

                    statement.setString(1, newStatus);
                    statement.setString(2, lockerNumber);

                    if (statement.executeUpdate() != 1) {
                        throw new SQLException(
                                "Could not update the locker status.");
                    }
                }

                connection.commit();

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
   }

    // Check active assignments against the semester-end date.
    public List<String> getOverdueAssignments(
            java.time.LocalDate semesterEndDate) throws SQLException {

        if (semesterEndDate == null) {
            throw new SQLException("Semester-end date is required.");
        }

        List<String> overdueAssignments = new ArrayList<>();

        // Only check assignments after the semester has ended.
        if (!java.time.LocalDate.now().isAfter(semesterEndDate)) {
            return overdueAssignments;
        }

        String sql =
                "SELECT a.assignment_id, s.student_id, s.name, "
                + "l.locker_number, a.assign_date "
                + "FROM assignments a "
                + "JOIN students s ON a.student_id = s.student_id "
                + "JOIN lockers l ON a.locker_number = l.locker_number "
                + "WHERE a.assign_date <= ? "
                + "ORDER BY a.assign_date, a.assignment_id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setDate(
                    1,
                    java.sql.Date.valueOf(semesterEndDate));

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    overdueAssignments.add(
                            "OVERDUE | Assignment #"
                            + result.getInt("assignment_id")
                            + " | " + result.getString("student_id")
                            + " - " + result.getString("name")
                            + " | Locker: " + result.getString("locker_number")
                            + " | Assigned: " + result.getDate("assign_date")
                            + " | Semester ended: " + semesterEndDate);
                }
            }
        }

        return overdueAssignments;
    }
}
