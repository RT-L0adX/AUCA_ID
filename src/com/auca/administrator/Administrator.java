package com.auca.administrator;

import com.auca.abstracts.Person;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import java.time.LocalDate;

public class Administrator extends Person {
    private String staffNumber;
    private String jobTitle;
    private String assignedOffice;
    private LocalDate employmentDate;
    private int accessLevel;
    private boolean active;

    public Administrator(String personId, String firstName, String lastName,
                         String email, String phoneNumber, LocalDate dateOfBirth,
                         String staffNumber, String jobTitle, String assignedOffice,
                         LocalDate employmentDate, int accessLevel, boolean active) {
        super(personId, firstName, lastName, email, phoneNumber, dateOfBirth);
        this.staffNumber = Registrable.clean(staffNumber);
        this.jobTitle = Registrable.clean(jobTitle);
        this.assignedOffice = Registrable.clean(assignedOffice);
        this.employmentDate = employmentDate;
        this.accessLevel = accessLevel;
        this.active = active;
    }

    public String getStaffNumber() { return staffNumber; }
    public void setStaffNumber(String staffNumber) { this.staffNumber = Registrable.clean(staffNumber); }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = Registrable.clean(jobTitle); }

    public String getAssignedOffice() { return assignedOffice; }
    public void setAssignedOffice(String assignedOffice) { this.assignedOffice = Registrable.clean(assignedOffice); }

    public LocalDate getEmploymentDate() { return employmentDate; }
    public void setEmploymentDate(LocalDate employmentDate) { this.employmentDate = employmentDate; }

    public int getAccessLevel() { return accessLevel; }
    public void setAccessLevel(int accessLevel) { this.accessLevel = accessLevel; }

    public void setActive(boolean active) { this.active = active; }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public String getRoleDescription() {
        return "Administrator";
    }

    @Override
    public void validate() throws ValidationException {
        super.validate();
        Registrable.requireLength("Staff number", staffNumber, Identifiable.MIN_ID_LENGTH, Identifiable.MAX_ID_LENGTH);
        Registrable.requireLength("Job title", jobTitle, 2, 120);
        Registrable.requireLength("Assigned office", assignedOffice, 2, 60);
        Registrable.requireNotFuture("Employment date", employmentDate);
        Registrable.requireRange("Access level", accessLevel, 1, 5);
    }

    @Override
    public void printDetails() {
        super.printDetails();
        System.out.println("Staff number  : " + staffNumber);
        System.out.println("Job title     : " + jobTitle);
        System.out.println("Office        : " + assignedOffice);
        System.out.println("Employed on   : " + employmentDate);
        System.out.println("Access level  : " + accessLevel);
        System.out.println("Active        : " + (active ? "YES" : "NO"));
    }

    @Override
    public String getRegistrationSummary() {
        return "Administrator [" + staffNumber + "] " + getDisplayName() + " - " + jobTitle;
    }

    @Override
    public String toString() {
        return "Administrator [staffNumber=" + staffNumber + ", name=" + getDisplayName()
                + ", jobTitle=" + jobTitle + ", assignedOffice=" + assignedOffice
                + ", employmentDate=" + employmentDate + ", accessLevel=" + accessLevel
                + ", active=" + active + "]";
    }
}