package com.auca.abstracts;

import com.auca.department.Department;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import java.time.LocalDate;

public abstract class AcademicMember extends Person {
    private String memberNumber;
    private Department department;
    private LocalDate joinDate;
    private String academicStatus;       
    private String institutionalEmail;
    private String advisorName;

    public AcademicMember(String personId, String firstName, String lastName,
                          String email, String phoneNumber, LocalDate dateOfBirth,
                          String memberNumber, Department department, LocalDate joinDate,
                          String academicStatus, String institutionalEmail, String advisorName) {
        super(personId, firstName, lastName, email, phoneNumber, dateOfBirth);   // constructor chaining
        this.memberNumber = Registrable.clean(memberNumber);
        this.department = department;
        this.joinDate = joinDate;
        this.academicStatus = Registrable.clean(academicStatus);
        this.institutionalEmail = Registrable.clean(institutionalEmail);
        this.advisorName = Registrable.clean(advisorName);
    }

    public String getMemberNumber() { return memberNumber; }
    public void setMemberNumber(String memberNumber) { this.memberNumber = Registrable.clean(memberNumber); }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public LocalDate getJoinDate() { return joinDate; }
    public void setJoinDate(LocalDate joinDate) { this.joinDate = joinDate; }

    public String getAcademicStatus() { return academicStatus; }
    public void setAcademicStatus(String academicStatus) { this.academicStatus = Registrable.clean(academicStatus); }

    public String getInstitutionalEmail() { return institutionalEmail; }
    public void setInstitutionalEmail(String institutionalEmail) {
        this.institutionalEmail = Registrable.clean(institutionalEmail);
    }

    public String getAdvisorName() { return advisorName; }
    public void setAdvisorName(String advisorName) { this.advisorName = Registrable.clean(advisorName); }

    public abstract String getAcademicCategory();

    @Override
    public boolean isActive() {
        return Identifiable.DEFAULT_STATUS.equals(academicStatus);
    }

    @Override
    public void validate() throws ValidationException {
        super.validate();
        Registrable.requireLength("Member number", memberNumber, Identifiable.MIN_ID_LENGTH, Identifiable.MAX_ID_LENGTH);
        Registrable.requireNotNull("Department", department);
        Registrable.requireNotFuture("Join date", joinDate);
        Registrable.requireOneOf("Academic status", academicStatus, new String[]{"ACTIVE", "INACTIVE"});
        Registrable.requireEmail("Institutional email", institutionalEmail);
        Registrable.requireLength("Advisor name", advisorName, 2, 120);
    }

    @Override
    public void printDetails() {
        super.printDetails();
        System.out.println("Member number : " + memberNumber);
        System.out.println("Department    : " + (department == null ? "none" : department.getDisplayName()));
        System.out.println("Join date     : " + joinDate);
        System.out.println("Status        : " + academicStatus);
        System.out.println("Inst. email   : " + institutionalEmail);
        System.out.println("Advisor       : " + advisorName);
    }

    @Override
    public String toString() {
        return super.toString() + " AcademicMember [memberNumber=" + memberNumber
                + ", department=" + (department == null ? "none" : department.getDisplayName())
                + ", joinDate=" + joinDate + ", academicStatus=" + academicStatus
                + ", institutionalEmail=" + institutionalEmail + ", advisorName=" + advisorName + "]";
    }
}