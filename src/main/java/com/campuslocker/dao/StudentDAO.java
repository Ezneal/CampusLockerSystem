
package com.campuslocker.dao;

import com.campuslocker.model.Student;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    public void addStudent(Student student) throws SQLException {
        String sql = "INSERT INTO students " +
                "(student_id, name, contact_number, grade_level, section) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, student.getStudentID());
            statement.setString(2, student.getName());
            statement.setString(3, student.getContactNumber());
            statement.setString(4, student.getGradeLevel());
            statement.setString(5, student.getSection());

            statement.executeUpdate();
        }
    }

    public List<Student> getAllStudents() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                Student student = new Student(
                        result.getString("name"),
                        result.getString("contact_number"),
                        result.getString("student_id"),
                        result.getString("grade_level"),
                        result.getString("section")
                );

                students.add(student);
            }
        }

        return students;
    }
}
