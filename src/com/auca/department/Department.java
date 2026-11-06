package com.auca.department;

import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import com.auca.teacher.Teacher;

public class Department implements Identifiable, Registrable {
    private String departmentId;
    private String departmentName;
    private String departmentCode;
    private String officeLocation;
    private String contactEmail;
    private Teacher headTeacher;       

    public Department(String departmentId, String departmentName, String departmentCode,
                      String officeLocation, String contactEmail, Teacher headTeacher) {
        this.departmentId = Registrable.clean(departmentId);
        this.departmentName = Registrable.clean(departmentName);
        this.departmentCode = departmentCode == null ? null : departmentCode.trim().toUpperCase();
        this.officeLocation = Registrable.clean(officeLocation);
        this.contactEmail = Registrable.clean(contactEmail);
        this.headTeacher = headTeacher;
    }

    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = Registrable.clean(departmentId); }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = Registrable.clean(departmentName); }

    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode == null ? null : departmentCode.trim().toUpperCase();
    }

    public String getOfficeLocation() { return officeLocation; }
    public void setOfficeLocation(String officeLocation) { this.officeLocation = Registrable.clean(officeLocation); }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = Registrable.clean(contactEmail); }

    public Teacher getHeadTeacher() { return headTeacher; }
    public void setHeadTeacher(Teacher headTeacher) { this.headTeacher = headTeacher; }

    @Override
    public String getId() {
        return departmentId;
    }

    @Override
    public String getDisplayName() {
        return departmentName;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void validate() throws ValidationException {
        Registrable.requireLength("Department ID", departmentId, Identifiable.MIN_ID_LENGTH, Identifiable.MAX_ID_LENGTH);
        Registrable.requireLength("Department name", departmentName, Registrable.MIN_NAME_LENGTH, Registrable.MAX_NAME_LENGTH);
        Registrable.requireLength("Department code", departmentCode, 2, 10);
        Registrable.requireLength("Office location", officeLocation, 2, 60);
        Registrable.requireEmail("Contact email", contactEmail);
        // headTeacher is optional, so it is not checked
    }

    @Override
    public void printDetails() {
        System.out.println("Department ID : " + departmentId);
        System.out.println("Name          : " + departmentName);
        System.out.println("Code          : " + departmentCode);
        System.out.println("Office        : " + officeLocation);
        System.out.println("Contact email : " + contactEmail);
        System.out.println("Head teacher  : " + (headTeacher == null ? "not assigned" : headTeacher.getDisplayName()));
    }

    @Override
    public String getRegistrationSummary() {
        return "[" + departmentId + "] " + departmentName + " (" + departmentCode + ")";
    }

    @Override
    public String toString() {
        return "Department [departmentId=" + departmentId + ", departmentName=" + departmentName
                + ", departmentCode=" + departmentCode + ", officeLocation=" + officeLocation
                + ", contactEmail=" + contactEmail + ", headTeacher="
                + (headTeacher == null ? "not assigned" : headTeacher.getDisplayName()) + "]";
    }
}