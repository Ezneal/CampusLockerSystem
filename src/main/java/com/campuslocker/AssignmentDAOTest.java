
package com.campuslocker;

import com.campuslocker.dao.AssignmentDAO;
import com.campuslocker.dao.StudentDAO;
import com.campuslocker.dao.LockerDAO;
import com.campuslocker.model.Student;
import com.campuslocker.model.Locker;

import java.sql.SQLException;
import java.util.List;

public class AssignmentDAOTest {

    public static void main(String[] args) {
        try {
            StudentDAO studentDAO = new StudentDAO();
            LockerDAO lockerDAO = new LockerDAO();
            AssignmentDAO assignmentDAO = new AssignmentDAO();

            // Find the existing student S001
            Student student = null;

            for (Student s : studentDAO.getAllStudents()) {
                if (s.getStudentID().equalsIgnoreCase("S001")) {
                    student = s;
                    break;
                }
            }

            // Find the existing locker L001
            Locker locker = null;

            for (Locker l : lockerDAO.getAllLockers()) {
                if (l.getLockerNumber().equalsIgnoreCase("L001")) {
                    locker = l;
                    break;
                }
            }

            if (student == null) {
                System.out.println("Student S001 was not found.");
                return;
            }

            if (locker == null) {
                System.out.println("Locker L001 was not found.");
                return;
            }

            if (!locker.isAvailable()) {
                System.out.println("Locker L001 is not available.");
                return;
            }

            // Assign the locker
            assignmentDAO.assignLocker(student, locker);

            System.out.println("Locker assigned successfully!");
            System.out.println("Student: " + student.getName());
            System.out.println("Locker: " + locker.getLockerNumber());
            System.out.println("Status: " + locker.getStatus().getStatusName());

            // Display saved assignments
            System.out.println("\nActive assignments:");

            List<String> assignments =
                    assignmentDAO.getActiveAssignments();

            for (String assignment : assignments) {
                System.out.println(assignment);
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}
