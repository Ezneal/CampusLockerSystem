
package com.campuslocker.ui;

import com.campuslocker.dao.StudentDAO;
import com.campuslocker.model.Student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class StudentManagementFrame extends JFrame {

    private final StudentDAO studentDAO = new StudentDAO();
    private final JTextField searchField = new JTextField(18);

    private final DefaultTableModel tableModel =
            new DefaultTableModel(
                    new String[]{
                            "Student ID",
                            "Name",
                            "Contact Number",
                            "Grade Level",
                            "Section"
                    }, 0) {

                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    private final JTable studentTable = new JTable(tableModel);

    public StudentManagementFrame() {
        setTitle("Student Management");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Student Management");
        title.setFont(new Font("Arial", Font.BOLD, 22));

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(
                BorderFactory.createEmptyBorder(15, 15, 5, 15));
        header.add(title, BorderLayout.WEST);

        JButton addButton = new JButton("Add Student");
        JButton refreshButton = new JButton("Refresh");
        JButton searchButton = new JButton("Search");
        JButton clearButton = new JButton("Clear");

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.setBorder(
                BorderFactory.createEmptyBorder(0, 15, 5, 15));

        controls.add(addButton);
        controls.add(refreshButton);
        controls.add(new JLabel("Search ID or Name:"));
        controls.add(searchField);
        controls.add(searchButton);
        controls.add(clearButton);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(header, BorderLayout.NORTH);
        topPanel.add(controls, BorderLayout.SOUTH);

        studentTable.setRowHeight(28);
        studentTable.setFillsViewportHeight(true);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(
                BorderFactory.createEmptyBorder(5, 15, 15, 15));
        tablePanel.add(new JScrollPane(studentTable));

        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);

        addButton.addActionListener(e -> addStudent());

        refreshButton.addActionListener(e -> {
            searchField.setText("");
            loadStudents();
        });

        searchButton.addActionListener(e -> searchStudents());

        clearButton.addActionListener(e -> {
            searchField.setText("");
            loadStudents();
        });

        searchField.addActionListener(e -> searchStudents());

        loadStudents();
    }

    private void loadStudents() {
        tableModel.setRowCount(0);

        try {
            List<Student> students = studentDAO.getAllStudents();

            for (Student student : students) {
                tableModel.addRow(new Object[]{
                        student.getStudentID(),
                        student.getName(),
                        student.getContactNumber(),
                        student.getGradeLevel(),
                        student.getSection()
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load students: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void searchStudents() {
        String keyword = searchField.getText().trim().toLowerCase();

        if (keyword.isEmpty()) {
            loadStudents();
            return;
        }

        try {
            List<Student> students = studentDAO.getAllStudents();

            tableModel.setRowCount(0);

            for (Student student : students) {
                boolean matchesID = student.getStudentID() != null
                        && student.getStudentID()
                                  .toLowerCase().contains(keyword);

                boolean matchesName = student.getName() != null
                        && student.getName()
                                  .toLowerCase().contains(keyword);

                if (matchesID || matchesName) {
                    tableModel.addRow(new Object[]{
                            student.getStudentID(),
                            student.getName(),
                            student.getContactNumber(),
                            student.getGradeLevel(),
                            student.getSection()
                    });
                }
            }

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "No students found matching: " + keyword,
                        "Search Results",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to search students: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void addStudent() {
        JTextField studentIDField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField contactField = new JTextField();
        JTextField gradeField = new JTextField();
        JTextField sectionField = new JTextField();

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));

        form.add(new JLabel("Student ID:"));
        form.add(studentIDField);

        form.add(new JLabel("Full Name:"));
        form.add(nameField);

        form.add(new JLabel("Contact Number:"));
        form.add(contactField);

        form.add(new JLabel("Grade Level:"));
        form.add(gradeField);

        form.add(new JLabel("Section:"));
        form.add(sectionField);

        int choice = JOptionPane.showConfirmDialog(
                this,
                form,
                "Add New Student",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        String studentID = studentIDField.getText().trim();
        String name = nameField.getText().trim();
        String contact = contactField.getText().trim();
        String grade = gradeField.getText().trim();
        String section = sectionField.getText().trim();

        if (studentID.isEmpty() || name.isEmpty()
                || contact.isEmpty() || grade.isEmpty()
                || section.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please fill in all fields.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        Student student = new Student(
                name, contact, studentID, grade, section);

        try {
            studentDAO.addStudent(student);

            JOptionPane.showMessageDialog(
                    this,
                    "Student added successfully!"
            );

            searchField.setText("");
            loadStudents();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to add student. Check whether the Student ID "
                            + "already exists.\n\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
