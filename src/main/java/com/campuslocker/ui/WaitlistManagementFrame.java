
package com.campuslocker.ui;

import com.campuslocker.dao.StudentDAO;
import com.campuslocker.dao.WaitlistDAO;
import com.campuslocker.model.Student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class WaitlistManagementFrame extends JFrame {

    private final StudentDAO studentDAO = new StudentDAO();
    private final WaitlistDAO waitlistDAO = new WaitlistDAO();

    private final JComboBox<Student> studentDropdown =
            new JComboBox<>();

    private final DefaultTableModel tableModel =
            new DefaultTableModel(
                    new String[]{"Queue Details"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    private final JTable waitlistTable = new JTable(tableModel);

    public WaitlistManagementFrame() {
        setTitle("Waitlist Management");
        setSize(700, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));

        JLabel title = new JLabel("Manage Student Waitlist");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(
                15, 15, 5, 15));
        header.add(title, BorderLayout.WEST);

     

        // Display student names and IDs in the dropdown.
        studentDropdown.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {

                super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);

                if (value instanceof Student) {
                    Student student = (Student) value;
                    setText(student.getName()
                            + " (" + student.getStudentID() + ")");
                }

                return this;
            }
        });

        JButton addButton = new JButton("Add to Waitlist");
        JButton refreshButton = new JButton("Refresh");
        JButton removeNextButton = new JButton("Remove Next Student");

        addButton.addActionListener(e -> addStudentToWaitlist());
        refreshButton.addActionListener(e -> loadData());
        removeNextButton.addActionListener(e -> removeNextStudent());

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.setBorder(BorderFactory.createEmptyBorder(
                0, 15, 0, 15));

        controls.add(new JLabel("Select Student:"));
        controls.add(studentDropdown);
        controls.add(addButton);
        controls.add(removeNextButton);
        controls.add(refreshButton);
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(header, BorderLayout.NORTH);
        topPanel.add(controls, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        

        waitlistTable.setRowHeight(28);
        waitlistTable.setFillsViewportHeight(true);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createEmptyBorder(
                0, 15, 15, 15));
        tablePanel.add(new JScrollPane(waitlistTable),
                BorderLayout.CENTER);

        add(tablePanel, BorderLayout.CENTER);

        loadData();
    }

    private void loadData() {
        try {
            studentDropdown.removeAllItems();

            List<Student> students = studentDAO.getAllStudents();

            for (Student student : students) {
                studentDropdown.addItem(student);
            }

            tableModel.setRowCount(0);

            List<String> waitingStudents =
                    waitlistDAO.getWaitingStudents();

            for (String student : waitingStudents) {
                tableModel.addRow(new Object[]{student});
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not load waitlist data:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addStudentToWaitlist() {
        Student student =
                (Student) studentDropdown.getSelectedItem();

        if (student == null) {
            JOptionPane.showMessageDialog(
                    this, "No students are available to select.");
            return;
        }

        try {
            waitlistDAO.addToWaitlist(student);

            JOptionPane.showMessageDialog(
                    this,
                    "Waitlist request processed for "
                            + student.getName() + ".");

            loadData();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not add student to waitlist:\n"
                            + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    private void removeNextStudent() {
    int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to remove the next student from the waitlist?",
            "Confirm Removal",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
    );

    if (confirm != JOptionPane.YES_OPTION) {
        return;
    }

    try {
        Student student = waitlistDAO.removeNextFromWaitlist();

        if (student == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "The waitlist is currently empty.",
                    "Empty Waitlist",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        JOptionPane.showMessageDialog(
                this,
                student.getName()
                        + " (" + student.getStudentID()
                        + ") has been removed from the waitlist.",
                "Removal Successful",
                JOptionPane.INFORMATION_MESSAGE
        );

        loadData();

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
                this,
                "Could not remove the next student:\n"
                        + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
    
}
