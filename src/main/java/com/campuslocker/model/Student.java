
package com.campuslocker.model;

public class Student extends Person {

    private String studentID;
    private String gradeLevel;
    private String section;

    public Student(
            String name,
            String contactNumber,
            String studentID,
            String gradeLevel,
            String section) {

        super(name, contactNumber);

        this.studentID = studentID;
        this.gradeLevel = gradeLevel;
        this.section = section;
    }

    public String getStudentID() {
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public String getGradeLevel() {
        return gradeLevel;
    }

    public void setGradeLevel(String gradeLevel) {
        this.gradeLevel = gradeLevel;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }
}
