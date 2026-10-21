
package com.campuslocker.model;

public class OccupiedStatus extends LockerStatus {

    private Student assignedStudent;

    public OccupiedStatus(Student assignedStudent) {
        this.assignedStudent = assignedStudent;
    }

    public Student getAssignedStudent() {
        return assignedStudent;
    }

    public void setAssignedStudent(Student assignedStudent) {
        this.assignedStudent = assignedStudent;
        updateDate();
    }

    @Override
    public String getStatusName() {
        return "Occupied";
    }

    @Override
    public String getStatusDetails() {
        if (assignedStudent == null) {
            return "Occupied, but no student record is linked.";
        }

        return "Assigned to: " + assignedStudent.getName();
    }
}
