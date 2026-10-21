
package com.campuslocker;

import com.campuslocker.dao.AssignmentDAO;
import java.sql.SQLException;

public class ReleaseLockerTest {

    public static void main(String[] args) {
        AssignmentDAO assignmentDAO = new AssignmentDAO();

        try {
            assignmentDAO.releaseLocker("L001");
            System.out.println("Locker release test completed!");

            System.out.println("\nRemaining active assignments:");

            for (String assignment : assignmentDAO.getActiveAssignments()) {
                System.out.println(assignment);
            }

        } catch (SQLException e) {
            System.out.println("Release failed: " + e.getMessage());
        }
    }
}
