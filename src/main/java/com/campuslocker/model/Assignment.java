
package com.campuslocker.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Assignment {

    private String assignmentID;
    private Student student;
    private Locker locker;
    private LocalDate assignDate;

    public Assignment(String assignmentID, Student student, Locker locker) {
        this.assignmentID = assignmentID;
        this.student = student;
        this.locker = locker;
        this.assignDate = LocalDate.now();
    }

    public String getAssignmentID() {
        return assignmentID;
    }

    public Student getStudent() {
        return student;
    }

    public Locker getLocker() {
        return locker;
    }

    public LocalDate getAssignDate() {
        return assignDate;
    }

    public boolean isOverdue(LocalDate semesterEndDate) {
        return LocalDate.now().isAfter(semesterEndDate);
    }

    public long getDaysUntilSemesterEnd(LocalDate semesterEndDate) {
        return ChronoUnit.DAYS.between(
            LocalDate.now(), semesterEndDate
        );
    }

    public void releaseAssignment() {
        locker.setStatus(new AvailableStatus());
    }
}
