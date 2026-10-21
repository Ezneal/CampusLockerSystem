
package com.campuslocker.dao;

import com.campuslocker.model.Locker;
import com.campuslocker.model.Student;
import com.campuslocker.model.OccupiedStatus;
import com.campuslocker.model.MaintenanceStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LockerDAO {

    public void addLocker(Locker locker) throws SQLException {
        String sql = "INSERT INTO lockers " +
                "(locker_number, location, status) VALUES (?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, locker.getLockerNumber());
            statement.setString(2, locker.getLocation());
            statement.setString(3, locker.getStatus().getStatusName());

            statement.executeUpdate();
        }
    }

    public void setLockerMaintenance(String lockerNumber, String issue)
            throws SQLException {

        String sql = "UPDATE lockers " +
                "SET status = ?, maintenance_issue = ? " +
                "WHERE locker_number = ? AND status = 'Available'";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, "Under Maintenance");
            statement.setString(2, issue);
            statement.setString(3, lockerNumber);

            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated == 0) {
                throw new SQLException(
                        "Locker not found or is not Available."
                );
            }
        }
    }

    public void setLockerAvailable(String lockerNumber)
            throws SQLException {

        String sql = "UPDATE lockers " +
                "SET status = ?, maintenance_issue = NULL " +
                "WHERE locker_number = ? AND status = 'Under Maintenance'";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, "Available");
            statement.setString(2, lockerNumber);

            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated == 0) {
                throw new SQLException(
                        "Locker not found or is not Under Maintenance."
                );
            }
        }
    }

    public List<Locker> getAllLockers() throws SQLException {
        List<Locker> lockers = new ArrayList<>();

        String sql =
                "SELECT l.locker_number, l.location, l.status, " +
                "l.maintenance_issue, " +
                "s.student_id, s.name, s.contact_number, " +
                "s.grade_level, s.section " +
                "FROM lockers l " +
                "LEFT JOIN assignments a " +
                "ON l.locker_number = a.locker_number " +
                "LEFT JOIN students s " +
                "ON a.student_id = s.student_id " +
                "ORDER BY l.locker_number";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                Locker locker = new Locker(
                        result.getString("locker_number"),
                        result.getString("location")
                );

                String status = result.getString("status");

                switch (status) {
                    case "Occupied":
                        Student student = null;
                        String studentID = result.getString("student_id");

                        if (studentID != null) {
                            student = new Student(
                                    result.getString("name"),
                                    result.getString("contact_number"),
                                    studentID,
                                    result.getString("grade_level"),
                                    result.getString("section")
                            );
                        }

                        if (student != null) {
                            locker.setStatus(new OccupiedStatus(student));
                        } else {
                            System.out.println(
                                    "Warning: Locker " + locker.getLockerNumber()
                                    + " is marked Occupied but has no linked student."
                            );

                            locker.setStatus(new OccupiedStatus(null));
                        }
                        break;

                    case "Under Maintenance":
                        String issue = result.getString("maintenance_issue");

                        if (issue == null || issue.trim().isEmpty()) {
                            issue = "No issue details provided";
                        }

                        locker.setStatus(new MaintenanceStatus(issue));
                        break;
                }

                lockers.add(locker);
            }
        }

        return lockers;
    }
}
