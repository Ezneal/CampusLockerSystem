
package com.campuslocker.model;

public class MaintenanceStatus extends LockerStatus {

    private String reportedIssue;

    public MaintenanceStatus(String reportedIssue) {
        this.reportedIssue = reportedIssue;
    }

    public String getReportedIssue() {
        return reportedIssue;
    }

    public void setReportedIssue(String reportedIssue) {
        this.reportedIssue = reportedIssue;
        updateDate();
    }

    @Override
    public String getStatusName() {
        return "Under Maintenance";
    }

    @Override
    public String getStatusDetails() {
        return "Reported issue: " + reportedIssue;
    }
}
