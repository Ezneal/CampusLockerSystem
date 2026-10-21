
package com.campuslocker.dao;

import com.campuslocker.model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WaitlistDAO {

   
public void addToWaitlist(Student student) throws SQLException {
    if (student == null) {
        throw new IllegalArgumentException(
                "Please select a student.");
    }

    String assignmentSQL =
            "SELECT COUNT(*) FROM assignments WHERE student_id = ?";

    String checkSQL =
            "SELECT COUNT(*) FROM waitlist WHERE student_id = ?";

    try (Connection connection = DatabaseConnection.getConnection()) {

        // Check whether the student already has a locker.
        try (PreparedStatement statement =
                     connection.prepareStatement(assignmentSQL)) {

            statement.setString(1, student.getStudentID());

            try (ResultSet result = statement.executeQuery()) {
                if (result.next() && result.getInt(1) > 0) {
                    throw new SQLException(
                            "This student already has a locker assignment.");
                }
            }
        }

        // Check whether the student is already waiting.
        try (PreparedStatement statement =
                     connection.prepareStatement(checkSQL)) {

            statement.setString(1, student.getStudentID());

            try (ResultSet result = statement.executeQuery()) {
                if (result.next() && result.getInt(1) > 0) {
                    throw new SQLException(
                            "This student is already on the waitlist.");
                }
            }
        }

        // Add the student to the waitlist.
        String insertSQL =
                "INSERT INTO waitlist (student_id) VALUES (?)";

        try (PreparedStatement statement =
                     connection.prepareStatement(insertSQL)) {

            statement.setString(1, student.getStudentID());
            statement.executeUpdate();
        }
    }
}


    public List<String> getWaitingStudents() throws SQLException {
        List<String> waitingStudents = new ArrayList<>();

        String sql =
                "SELECT w.queue_id, s.student_id, s.name, w.request_date " +
                "FROM waitlist w " +
                "JOIN students s ON w.student_id = s.student_id " +
                "ORDER BY w.queue_id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                waitingStudents.add(
                        "Queue #" + result.getInt("queue_id") +
                        " | " + result.getString("student_id") +
                        " - " + result.getString("name") +
                        " | Requested: " + result.getTimestamp("request_date")
                );
            }
        }

        return waitingStudents;
    }

    public Student removeNextFromWaitlist() throws SQLException {
        String sql =
                "SELECT s.student_id, s.name, s.contact_number, " +
                "s.grade_level, s.section " +
                "FROM waitlist w " +
                "JOIN students s ON w.student_id = s.student_id " +
                "ORDER BY w.queue_id LIMIT 1";

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                String studentID = null;
                Student student = null;

                try (PreparedStatement statement =
                             connection.prepareStatement(sql);
                     ResultSet result = statement.executeQuery()) {

                    if (result.next()) {
                        studentID = result.getString("student_id");

                        student = new Student(
                                result.getString("name"),
                                result.getString("contact_number"),
                                studentID,
                                result.getString("grade_level"),
                                result.getString("section")
                        );
                    }
                }

                if (student != null) {
                    String deleteSQL =
                            "DELETE FROM waitlist WHERE student_id = ? " +
                            "ORDER BY queue_id LIMIT 1";

                    try (PreparedStatement delete =
                                 connection.prepareStatement(deleteSQL)) {
                        delete.setString(1, studentID);
                        delete.executeUpdate();
                    }
                }

                connection.commit();
                return student;

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }
}
