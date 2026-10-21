
package com.campuslocker.ui;

import com.campuslocker.dao.AssignmentDAO;
import com.campuslocker.dao.LockerDAO;
import com.campuslocker.dao.StudentDAO;
import com.campuslocker.model.Locker;
import com.campuslocker.model.Student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import javax.swing.table.TableRowSorter;
import java.util.regex.Pattern;

public class LockerManagementFrame extends JFrame {

    private final LockerDAO lockerDAO = new LockerDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final AssignmentDAO assignmentDAO = new AssignmentDAO();

    private final JComboBox<Student> studentCombo = new JComboBox<>();
    private final JComboBox<Locker> lockerCombo = new JComboBox<>();

    private final DefaultTableModel tableModel =
            new DefaultTableModel(
                    new String[]{"Locker Number", "Location", "Status", "Details"},
                    0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    private final JTable lockerTable = new JTable(tableModel);

    private final JTextField lockerSearchField = new JTextField(15);
    private final TableRowSorter<DefaultTableModel> sorter =
            new TableRowSorter<>(tableModel);

    public LockerManagementFrame() {
        setTitle("Locker Management");
        setSize(900, 650);
        setMinimumSize(new Dimension(750, 520));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(12, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(245, 248, 255));
        setContentPane(mainPanel);

        JLabel heading = new JLabel("Locker Management");
        heading.setFont(new Font("SansSerif", Font.BOLD, 25));
        heading.setForeground(new Color(35, 65, 115));

        JLabel subtitle = new JLabel(
                "Assign, release, and manage locker statuses");

        JPanel headingPanel = new JPanel();
        headingPanel.setOpaque(false);
        headingPanel.setLayout(new BoxLayout(headingPanel, BoxLayout.Y_AXIS));
        headingPanel.add(heading);
        headingPanel.add(Box.createVerticalStrut(5));
        headingPanel.add(subtitle);

        mainPanel.add(headingPanel, BorderLayout.NORTH);

        lockerTable.setRowHeight(28);
        lockerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lockerTable.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 13));

        lockerTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(lockerTable);

        JPanel searchPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);

        JButton searchButton = new JButton("Search");
        JButton clearSearchButton = new JButton("Clear");

        searchPanel.add(new JLabel("Locker Number:"));
        searchPanel.add(lockerSearchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearSearchButton);

        JPanel inventoryPanel = new JPanel(new BorderLayout(8, 8));
        inventoryPanel.setOpaque(false);
        inventoryPanel.add(searchPanel, BorderLayout.NORTH);
        inventoryPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(inventoryPanel, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new GridBagLayout());
        actionPanel.setBackground(Color.WHITE);
        actionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 228, 240)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        gbc.gridx = 0;
        gbc.gridy = 0;
        actionPanel.add(new JLabel("Student:"), gbc);

        gbc.gridx = 1;
        actionPanel.add(new JLabel("Available locker:"), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        actionPanel.add(studentCombo, gbc);

        gbc.gridx = 1;
        actionPanel.add(lockerCombo, gbc);

        JButton assignButton = new JButton("Assign Locker");
        JButton releaseButton = new JButton("Release Selected Locker");
        JButton maintenanceButton = new JButton("Set to Maintenance");
        JButton restoreButton = new JButton("Restore to Available");
        JButton refreshButton = new JButton("Refresh");

        gbc.gridx = 0;
        gbc.gridy = 2;
        actionPanel.add(assignButton, gbc);

        gbc.gridx = 1;
        actionPanel.add(releaseButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        actionPanel.add(maintenanceButton, gbc);

        gbc.gridx = 1;
        actionPanel.add(restoreButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        actionPanel.add(refreshButton, gbc);

        mainPanel.add(actionPanel, BorderLayout.SOUTH);

        // Display readable student names in the dropdown.
        studentCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {

                super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);

                if (value instanceof Student student) {
                    setText(student.getName() + " ("
                            + student.getStudentID() + ")");
                }

                return this;
            }
        });

        // Display locker number, location, and status in the dropdown.
        lockerCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {

                super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);

                if (value instanceof Locker locker) {
                    setText(locker.getLockerNumber() + " - "
                            + locker.getLocation() + " ("
                            + locker.getStatus().getStatusName() + ")");
                }

                return this;
            }
        });

        assignButton.addActionListener(e -> assignSelectedLocker());
        releaseButton.addActionListener(e -> releaseSelectedLocker());
        maintenanceButton.addActionListener(
                e -> setSelectedLockerMaintenance());
        restoreButton.addActionListener(
                e -> restoreSelectedLocker());
        searchButton.addActionListener(e -> filterLockers());

        lockerSearchField.addActionListener(e -> filterLockers());

        clearSearchButton.addActionListener(e -> {
            lockerSearchField.setText("");
            sorter.setRowFilter(null);
        });

        refreshButton.addActionListener(e -> {
            lockerSearchField.setText("");
            sorter.setRowFilter(null);
            loadData();
        });
        

        loadData();
    }

    private void loadData() {
        try {
            List<Locker> lockers = lockerDAO.getAllLockers();
            List<Student> students = studentDAO.getAllStudents();

            tableModel.setRowCount(0);
            lockerCombo.removeAllItems();
            studentCombo.removeAllItems();

            for (Locker locker : lockers) {
                String status = locker.getStatus().getStatusName();

                tableModel.addRow(new Object[]{
                        locker.getLockerNumber(),
                        locker.getLocation(),
                        status,
                        locker.getStatusDetails()
                });

                // Only available lockers can be assigned.
                if (locker.isAvailable()) {
                    lockerCombo.addItem(locker);
                }
            }

            for (Student student : students) {
                studentCombo.addItem(student);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not load data from the database.\n"
                            + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    private void filterLockers() {
    String keyword = lockerSearchField.getText().trim();

    if (keyword.isEmpty()) {
        sorter.setRowFilter(null);
        return;
    }

    sorter.setRowFilter(
            RowFilter.regexFilter(
                    "(?i)" + Pattern.quote(keyword), 0));
    }

    private void assignSelectedLocker() {
        Student student = (Student) studentCombo.getSelectedItem();
        Locker locker = (Locker) lockerCombo.getSelectedItem();

        if (student == null) {
            JOptionPane.showMessageDialog(
                    this, "Please select a student.");
            return;
        }

        if (locker == null) {
            JOptionPane.showMessageDialog(
                    this, "There are no available lockers to assign.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Assign locker " + locker.getLockerNumber()
                        + " to " + student.getName() + "?",
                "Confirm Assignment",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            assignmentDAO.assignLocker(student, locker);

            JOptionPane.showMessageDialog(
                    this, "Locker assigned successfully!");

            loadData();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not assign locker.\n" + e.getMessage(),
                    "Assignment Error",
                    JOptionPane.ERROR_MESSAGE);

            loadData();
        }
    }

    private void releaseSelectedLocker() {
        int selectedRow = lockerTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this, "Please select a locker from the table first.");
            return;
        }

        int row = lockerTable.convertRowIndexToModel(selectedRow);

        String lockerNumber =
                tableModel.getValueAt(row, 0).toString();

        String status =
                tableModel.getValueAt(row, 2).toString();

        if (!"Occupied".equals(status)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Only occupied lockers can be released.",
                    "Cannot Release Locker",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Release locker " + lockerNumber
                        + "?\nIf students are waiting, the first eligible "
                        + "student on the waitlist may receive this locker.",
                "Confirm Release",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            assignmentDAO.releaseLocker(lockerNumber);

            JOptionPane.showMessageDialog(
                    this, "Locker released successfully.");

            loadData();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not release locker.\n" + e.getMessage(),
                    "Release Error",
                    JOptionPane.ERROR_MESSAGE);

            loadData();
        }
    }

    private void setSelectedLockerMaintenance() {
        int selectedRow = lockerTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a locker from the table first.");
            return;
        }

        int row = lockerTable.convertRowIndexToModel(selectedRow);

        String lockerNumber =
                tableModel.getValueAt(row, 0).toString();

        String status =
                tableModel.getValueAt(row, 2).toString();

        if (!"Available".equals(status)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Only available lockers can be placed under maintenance.",
                    "Cannot Set Maintenance",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String issue = JOptionPane.showInputDialog(
                this,
                "Describe the maintenance issue for locker "
                        + lockerNumber + ":",
                "Locker Maintenance",
                JOptionPane.QUESTION_MESSAGE);

        if (issue == null) {
            return;
        }

        issue = issue.trim();

        if (issue.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a description of the issue.",
                    "Issue Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Place locker " + lockerNumber
                        + " under maintenance?",
                "Confirm Maintenance",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            lockerDAO.setLockerMaintenance(lockerNumber, issue);

            JOptionPane.showMessageDialog(
                    this,
                    "Locker " + lockerNumber
                            + " is now under maintenance.");

            loadData();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not update locker status.\n" + e.getMessage(),
                    "Maintenance Error",
                    JOptionPane.ERROR_MESSAGE);

            loadData();
        }
    }

    private void restoreSelectedLocker() {
        int selectedRow = lockerTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a locker from the table first.");
            return;
        }

        int row = lockerTable.convertRowIndexToModel(selectedRow);

        String lockerNumber =
                tableModel.getValueAt(row, 0).toString();

        String status =
                tableModel.getValueAt(row, 2).toString();

        if (!"Under Maintenance".equals(status)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a locker that is under maintenance.",
                    "Cannot Restore Locker",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Mark locker " + lockerNumber
                        + " as Available after repairs?",
                "Restore Locker",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            lockerDAO.setLockerAvailable(lockerNumber);

            JOptionPane.showMessageDialog(
                    this,
                    "Locker " + lockerNumber
                            + " is now available.");

            loadData();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not restore locker.\n" + e.getMessage(),
                    "Restore Error",
                    JOptionPane.ERROR_MESSAGE);

            loadData();
        }
    }
}
