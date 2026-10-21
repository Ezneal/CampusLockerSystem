
package com.campuslocker;

import com.campuslocker.dao.StudentDAO;
import com.campuslocker.dao.WaitlistDAO;
import com.campuslocker.model.Student;

import java.sql.SQLException;

public class WaitlistDAOTest {

    public static void main(String[] args) {
        try {
            StudentDAO studentDAO = new StudentDAO();
            WaitlistDAO waitlistDAO = new WaitlistDAO();

            Student waitingStudent = null;

            for (Student student : studentDAO.getAllStudents()) {
                if (student.getStudentID().equalsIgnoreCase("S002")) {
                    waitingStudent = student;
                    break;
                }
            }

            if (waitingStudent == null) {
                System.out.println("Student S002 was not found.");
                return;
            }

            waitlistDAO.addToWaitlist(waitingStudent);

            System.out.println("Student added to waitlist!");

            System.out.println("\nCurrent waitlist:");

            for (String student : waitlistDAO.getWaitingStudents()) {
                System.out.println(student);
            }

        } catch (SQLException e) {
            System.out.println("Test failed: " + e.getMessage());
        }
    }
}
