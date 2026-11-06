package com.auca.enrollment;

import com.auca.course.Course;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import com.auca.student.Student;
import java.time.LocalDate;

public class Enrollment implements Identifiable, Registrable {
    private String enrollmentId;
    private Student student;
    private Course course;
    private LocalDate enrollmentDate;
    private String semester;             
    private String enrollmentStatus;     

    public Enrollment(String enrollmentId, Student student, Course course,
                      LocalDate enrollmentDate, String semester, String enrollmentStatus) {
        this.enrollmentId = Registrable.clean(enrollmentId);
        this.student = student;
        this.course = course;
        this.enrollmentDate = enrollmentDate;
        this.semester = Registrable.clean(semester);
        this.enrollmentStatus = Registrable.clean(enrollmentStatus);
    }

    public String getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(String enrollmentId) { this.enrollmentId = Registrable.clean(enrollmentId); }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public LocalDate getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDate enrollmentDate) { this.enrollmentDate = enrollmentDate; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = Registrable.clean(semester); }

    public String getEnrollmentStatus() { return enrollmentStatus; }
    public void setEnrollmentStatus(String enrollmentStatus) { this.enrollmentStatus = Registrable.clean(enrollmentStatus); }

    @Override
    public String getId() {
        return enrollmentId;
    }

    @Override
    public String getDisplayName() {
        return student.getDisplayName() + " in " + course.getCourseCode() + " (" + semester + ")";
    }

    @Override
    public boolean isActive() {
        return Identifiable.DEFAULT_STATUS.equals(enrollmentStatus);
    }

    @Override
    public void validate() throws ValidationException {
        Registrable.requireLength("Enrollment ID", enrollmentId, Identifiable.MIN_ID_LENGTH, Identifiable.MAX_ID_LENGTH);
        Registrable.requireNotNull("Student", student);
        Registrable.requireNotNull("Course", course);
        Registrable.requireNotFuture("Enrollment date", enrollmentDate);
        Registrable.requireOneOf("Semester", semester, new String[]{"FALL", "SPRING", "SUMMER"});
        Registrable.requireOneOf("Enrollment status", enrollmentStatus, new String[]{"ACTIVE", "COMPLETED", "DROPPED"});
    }

    @Override
    public void printDetails() {
        System.out.println("Enrollment ID : " + enrollmentId);
        System.out.println("Student       : " + (student == null ? "none" : student.getDisplayName()));
        System.out.println("Course        : " + (course == null ? "none" : course.getDisplayName()));
        System.out.println("Date          : " + enrollmentDate);
        System.out.println("Semester      : " + semester);
        System.out.println("Status        : " + enrollmentStatus);
    }

    @Override
    public String getRegistrationSummary() {
        return "[" + enrollmentId + "] " + getDisplayName() + " - " + enrollmentStatus;
    }

    @Override
    public String toString() {
        return "Enrollment [enrollmentId=" + enrollmentId + ", student=" + student.getDisplayName()
                + ", course=" + course.getCourseCode() + ", enrollmentDate=" + enrollmentDate
                + ", semester=" + semester + ", enrollmentStatus=" + enrollmentStatus + "]";
    }
}