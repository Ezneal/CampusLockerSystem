
package com.campuslocker.ui;

import com.campuslocker.dao.LockerDAO;
import com.campuslocker.model.Locker;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class DashboardFrame extends JFrame {

    private final LockerDAO lockerDAO = new LockerDAO();

    private final JLabel totalLabel = new JLabel("0");
    private final JLabel availableLabel = new JLabel("0");
    private final JLabel occupiedLabel = new JLabel("0");
    private final JLabel maintenanceLabel = new JLabel("0");

    private final DefaultTableModel tableModel =
            new DefaultTableModel(
                    new String[]{"Locker Number", "Location", "Status", "Details"},
                    0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    public DashboardFrame() {
        setTitle("Campus Locker Allocation System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 620);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(new Color(245, 248, 255));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setContentPane(mainPanel);

        JLabel heading = new JLabel("Campus Locker Dashboard");
        heading.setFont(new Font("SansSerif", Font.BOLD, 26));
        heading.setForeground(new Color(35, 65, 115));

        JLabel subtitle = new JLabel("Locker inventory and current status");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JPanel headingPanel = new JPanel();
        headingPanel.setOpaque(false);
        headingPanel.setLayout(new BoxLayout(headingPanel, BoxLayout.Y_AXIS));
        headingPanel.add(heading);
        headingPanel.add(Box.createVerticalStrut(5));
        headingPanel.add(subtitle);

        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        summaryPanel.setOpaque(false);
        summaryPanel.add(createSummaryCard("Total Lockers", totalLabel));
        summaryPanel.add(createSummaryCard("Available", availableLabel));
        summaryPanel.add(createSummaryCard("Occupied", occupiedLabel));
        summaryPanel.add(createSummaryCard("Maintenance", maintenanceLabel));

        JPanel topPanel = new JPanel(new BorderLayout(0, 18));
        topPanel.setOpaque(false);
        topPanel.add(headingPanel, BorderLayout.NORTH);
        topPanel.add(summaryPanel, BorderLayout.CENTER);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        JTable lockerTable = new JTable(tableModel);
        lockerTable.setRowHeight(30);
        lockerTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lockerTable.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane(lockerTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel tablePanel = new JPanel(new BorderLayout(0, 10));
        tablePanel.setOpaque(false);

        JLabel tableHeading = new JLabel("Locker Inventory");
        tableHeading.setFont(new Font("SansSerif", Font.BOLD, 18));
        
        JButton overdueButton = new JButton("Overdue Assignments");

        overdueButton.addActionListener(e -> {
            OverdueAssignmentsFrame frame =
                    new OverdueAssignmentsFrame();
            frame.setVisible(true);
        });

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadLockers());

        JButton manageStudentsButton =
                new JButton("Manage Students");

        manageStudentsButton.addActionListener(e -> {
            StudentManagementFrame frame =
                new StudentManagementFrame();
            frame.setVisible(true);
        });

        JButton manageLockersButton = new JButton("Manage Lockers");
        manageLockersButton.addActionListener(e -> {
            LockerManagementFrame frame = new LockerManagementFrame();
            frame.setVisible(true);
        });
        JButton manageWaitlistButton =
                new JButton("Manage Waitlist");

        manageWaitlistButton.addActionListener(e -> {
            WaitlistManagementFrame frame =
                    new WaitlistManagementFrame();
            frame.setVisible(true);
        });

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        tableHeader.add(tableHeading, BorderLayout.WEST);
        JPanel headerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerButtons.setOpaque(false);
        headerButtons.add(manageStudentsButton);
        headerButtons.add(manageLockersButton);
        headerButtons.add(manageWaitlistButton);
        headerButtons.add(overdueButton);
        headerButtons.add(refreshButton);
        
        tableHeader.add(headerButtons, BorderLayout.EAST);

        tablePanel.add(tableHeader, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(tablePanel, BorderLayout.CENTER);

        loadLockers();
    }

    private JPanel createSummaryCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 228, 240)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        titleLabel.setForeground(new Color(90, 100, 120));

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        valueLabel.setForeground(new Color(35, 65, 115));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private void loadLockers() {
        try {
            List<Locker> lockers = lockerDAO.getAllLockers();

            tableModel.setRowCount(0);

            int available = 0;
            int occupied = 0;
            int maintenance = 0;

            for (Locker locker : lockers) {
                String status = locker.getStatus().getStatusName();

                switch (status) {
                    case "Available" -> available++;
                    case "Occupied" -> occupied++;
                    case "Under Maintenance" -> maintenance++;
                }

                tableModel.addRow(new Object[]{
                        locker.getLockerNumber(),
                        locker.getLocation(),
                        status,
                        locker.getStatusDetails()
                });
            }

            totalLabel.setText(String.valueOf(lockers.size()));
            availableLabel.setText(String.valueOf(available));
            occupiedLabel.setText(String.valueOf(occupied));
            maintenanceLabel.setText(String.valueOf(maintenance));

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not load lockers from the database.\n"
                            + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
