package com.auca.course;

import com.auca.department.Department;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import com.auca.teacher.Teacher;

public class Course implements Identifiable, Registrable {
    private String courseId;
    private String courseCode;
    private String courseTitle;
    private int creditHours;
    private Department department;
    private Teacher assignedTeacher;

    public Course(String courseId, String courseCode, String courseTitle,
                  int creditHours, Department department, Teacher assignedTeacher) {
        this.courseId = Registrable.clean(courseId);
        this.courseCode = courseCode == null ? null : courseCode.trim().toUpperCase();
        this.courseTitle = Registrable.clean(courseTitle);
        this.creditHours = creditHours;
        this.department = department;
        this.assignedTeacher = assignedTeacher;
    }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = Registrable.clean(courseId); }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode == null ? null : courseCode.trim().toUpperCase();
    }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = Registrable.clean(courseTitle); }

    public int getCreditHours() { return creditHours; }
    public void setCreditHours(int creditHours) { this.creditHours = creditHours; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public Teacher getAssignedTeacher() { return assignedTeacher; }
    public void setAssignedTeacher(Teacher assignedTeacher) { this.assignedTeacher = assignedTeacher; }

    @Override
    public String getId() {
        return courseId;
    }

    @Override
    public String getDisplayName() {
        return courseCode + " - " + courseTitle;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void validate() throws ValidationException {
        Registrable.requireLength("Course ID", courseId, Identifiable.MIN_ID_LENGTH, Identifiable.MAX_ID_LENGTH);
        Registrable.requireLength("Course code", courseCode, 2, 20);
        Registrable.requireLength("Course title", courseTitle, 2, 120);
        Registrable.requireRange("Credit hours", creditHours, 1, 6);
        Registrable.requireNotNull("Department", department);
        Registrable.requireNotNull("Assigned teacher", assignedTeacher);
    }

    @Override
    public void printDetails() {
        System.out.println("Course ID     : " + courseId);
        System.out.println("Code          : " + courseCode);
        System.out.println("Title         : " + courseTitle);
        System.out.println("Credit hours  : " + creditHours);
        System.out.println("Department    : " + (department == null ? "none" : department.getDisplayName()));
        System.out.println("Teacher       : " + (assignedTeacher == null ? "none" : assignedTeacher.getDisplayName()));
    }

    @Override
    public String getRegistrationSummary() {
        return "[" + courseId + "] " + getDisplayName() + " (" + creditHours + " credits)";
    }

    @Override
    public String toString() {
        return "Course [courseId=" + courseId + ", courseCode=" + courseCode + ", courseTitle=" + courseTitle
                + ", creditHours=" + creditHours
                + ", department=" + (department == null ? "none" : department.getDisplayName())
                + ", assignedTeacher=" + (assignedTeacher == null ? "none" : assignedTeacher.getDisplayName()) + "]";
    }
}