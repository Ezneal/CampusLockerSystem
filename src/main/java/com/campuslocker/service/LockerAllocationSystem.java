
package com.campuslocker.service;

import com.campuslocker.model.*;
import java.util.ArrayList;
import java.util.List;

public class LockerAllocationSystem {

    private ArrayList<Locker> lockers;
    private ArrayList<Student> students;
    private ArrayList<Assignment> assignments;
    private Waitlist waitlist;
    private int nextAssignmentID = 1;

    public LockerAllocationSystem() {
        lockers = new ArrayList<>();
        students = new ArrayList<>();
        assignments = new ArrayList<>();
        waitlist = new Waitlist();
    }

    public void addLocker(Locker locker) {
        for (Locker existing : lockers) {
            if (existing.getLockerNumber().equalsIgnoreCase(
                    locker.getLockerNumber())) {
                throw new IllegalArgumentException(
                    "Locker number already exists.");
            }
        }
        lockers.add(locker);
    }

    public void registerStudent(Student student) {
        for (Student existing : students) {
            if (existing.getStudentID().equalsIgnoreCase(
                    student.getStudentID())) {
                throw new IllegalArgumentException(
                    "Student ID already exists.");
            }
        }
        students.add(student);
    }

    public Assignment assignLocker(Student student) {
        if (!students.contains(student)) {
            throw new IllegalArgumentException(
                "Please register the student first.");
        }

        for (Assignment assignment : assignments) {
            if (assignment.getStudent().equals(student)) {
                throw new IllegalStateException(
                    "Student already has an assignment.");
            }
        }

        for (Locker locker : lockers) {
            if (locker.isAvailable()) {
                locker.setStatus(new OccupiedStatus(student));

                Assignment assignment = new Assignment(
                    String.valueOf(nextAssignmentID++),
                    student,
                    locker
                );

                assignments.add(assignment);
                return assignment;
            }
        }

        waitlist.addToQueue(student);
        return null;
    }

    public void releaseLocker(String lockerNumber) {
        for (Assignment assignment : new ArrayList<>(assignments)) {
            Locker locker = assignment.getLocker();

            if (locker.getLockerNumber().equalsIgnoreCase(lockerNumber)) {
                assignment.releaseAssignment();
                assignments.remove(assignment);
                assignNextWaitingStudent(locker);
                return;
            }
        }

        throw new IllegalArgumentException(
            "No active assignment found for that locker.");
    }

    private void assignNextWaitingStudent(Locker locker) {
        while (!waitlist.isEmpty()) {
            Student nextStudent = waitlist.removeFromQueue();

            if (nextStudent != null && hasActiveAssignment(nextStudent)) {
                continue;
            }

            if (nextStudent != null) {
                locker.setStatus(new OccupiedStatus(nextStudent));

                Assignment assignment = new Assignment(
                    String.valueOf(nextAssignmentID++),
                    nextStudent,
                    locker
                );

                assignments.add(assignment);
                break;
            }
        }
    }

    private boolean hasActiveAssignment(Student student) {
        for (Assignment assignment : assignments) {
            if (assignment.getStudent().equals(student)) {
                return true;
            }
        }
        return false;
    }

    public List<Locker> getLockers() {
        return new ArrayList<>(lockers);
    }

    public List<Student> getStudents() {
        return new ArrayList<>(students);
    }

    public List<Assignment> getAssignments() {
        return new ArrayList<>(assignments);
    }

    public Waitlist getWaitlist() {
        return waitlist;
    }

    public List<Locker> searchLocker(String keyword) {
        List<Locker> results = new ArrayList<>();

        for (Locker locker : lockers) {
            if (locker.getLockerNumber().toLowerCase()
                    .contains(keyword.toLowerCase())
                || locker.getLocation().toLowerCase()
                    .contains(keyword.toLowerCase())) {
                results.add(locker);
            }
        }

        return results;
    }
}
