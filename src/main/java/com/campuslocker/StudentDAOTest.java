
package com.campuslocker;

import com.campuslocker.dao.StudentDAO;
import com.campuslocker.model.Student;

public class StudentDAOTest {

    public static void main(String[] args) {
        StudentDAO studentDAO = new StudentDAO();

        try {
            Student student = new Student(
                    "Alex", "09123456789",
                    "S001", "Grade 11", "A"
            );

            studentDAO.addStudent(student);
            System.out.println("Student saved successfully!");

            System.out.println("\nRegistered students:");

            for (Student s : studentDAO.getAllStudents()) {
                System.out.println(
                        s.getStudentID() + " - " +
                        s.getName() + " - " +
                        s.getGradeLevel() + " - " +
                        s.getSection()
                );
            }

        } catch (Exception e) {
            System.out.println("Something went wrong:");
            System.out.println(e.getMessage());
        }
    }
}
