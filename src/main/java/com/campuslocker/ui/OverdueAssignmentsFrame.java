
package com.campuslocker.ui;

import com.campuslocker.dao.AssignmentDAO;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class OverdueAssignmentsFrame extends JFrame {

    private final AssignmentDAO assignmentDAO = new AssignmentDAO();

    private final JTextField semesterEndField =
            new JTextField(10);

    private final DefaultListModel<String> listModel =
            new DefaultListModel<>();

    private final JList<String> overdueList =
            new JList<>(listModel);

    public OverdueAssignmentsFrame() {
        setTitle("Overdue Locker Assignments");
        setSize(750, 450);
        setMinimumSize(new Dimension(600, 350));
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(245, 248, 255));
        setContentPane(mainPanel);

        JLabel heading =
                new JLabel("Overdue Locker Assignments");
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        heading.setForeground(new Color(35, 65, 115));

        JLabel instructions = new JLabel(
                "Enter the semester-end date to check active assignments.");
        instructions.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel headingPanel = new JPanel();
        headingPanel.setOpaque(false);
        headingPanel.setLayout(
                new BoxLayout(headingPanel, BoxLayout.Y_AXIS));
        headingPanel.add(heading);
        headingPanel.add(Box.createVerticalStrut(6));
        headingPanel.add(instructions);

        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.setOpaque(false);
        datePanel.add(new JLabel("Semester-end date (YYYY-MM-DD):"));
        datePanel.add(semesterEndField);

        JButton checkButton = new JButton("Check Overdue");
        checkButton.addActionListener(e -> checkOverdueAssignments());
        datePanel.add(checkButton);

        overdueList.setFont(new Font("SansSerif", Font.PLAIN, 13));
        overdueList.setVisibleRowCount(10);

        JScrollPane scrollPane = new JScrollPane(overdueList);

        JPanel topPanel = new JPanel(new BorderLayout(0, 12));
        topPanel.setOpaque(false);
        topPanel.add(headingPanel, BorderLayout.NORTH);
        topPanel.add(datePanel, BorderLayout.SOUTH);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void checkOverdueAssignments() {
        String dateText = semesterEndField.getText().trim();

        try {
            LocalDate semesterEndDate = LocalDate.parse(dateText);

            List<String> overdueAssignments =
                    assignmentDAO.getOverdueAssignments(semesterEndDate);

            listModel.clear();

            if (overdueAssignments.isEmpty()) {
                listModel.addElement(
                        LocalDate.now().isAfter(semesterEndDate)
                        ? "No active assignments found."
                        : "The semester-end date has not passed yet.");
            } else {
                for (String assignment : overdueAssignments) {
                    listModel.addElement(assignment);
                }
            }

        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid date in YYYY-MM-DD format.\n"
                    + "Example: 2026-10-31",
                    "Invalid Date",
                    JOptionPane.WARNING_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not check assignments.\n" + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
