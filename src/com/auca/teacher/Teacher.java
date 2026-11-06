package com.auca.teacher;

import com.auca.abstracts.AcademicMember;
import com.auca.department.Department;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class Teacher extends AcademicMember {
    private String employeeNumber;
    private String specialization;
    private LocalDate hireDate;
    private BigDecimal salary;
    private String officeNumber;
    private String employmentType;     

    public Teacher(String personId, String firstName, String lastName,
                   String email, String phoneNumber, LocalDate dateOfBirth,
                   String memberNumber, Department department, LocalDate joinDate,
                   String academicStatus, String institutionalEmail, String advisorName,
                   String employeeNumber, String specialization, LocalDate hireDate,
                   BigDecimal salary, String officeNumber, String employmentType) {
        super(personId, firstName, lastName, email, phoneNumber, dateOfBirth,
                memberNumber, department, joinDate, academicStatus, institutionalEmail, advisorName);
        this.employeeNumber = Registrable.clean(employeeNumber);
        this.specialization = Registrable.clean(specialization);
        this.hireDate = hireDate;
        this.salary = salary;
        this.officeNumber = Registrable.clean(officeNumber);
        this.employmentType = Registrable.clean(employmentType);
    }

    public String getEmployeeNumber() { return employeeNumber; }
    public void setEmployeeNumber(String employeeNumber) { this.employeeNumber = Registrable.clean(employeeNumber); }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = Registrable.clean(specialization); }

    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }

    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }

    public String getOfficeNumber() { return officeNumber; }
    public void setOfficeNumber(String officeNumber) { this.officeNumber = Registrable.clean(officeNumber); }

    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = Registrable.clean(employmentType); }

    @Override
    public String getRoleDescription() {
        return "Teacher";
    }

    @Override
    public String getAcademicCategory() {
        return "Teaching Staff";
    }

    @Override
    public void validate() throws ValidationException {
        super.validate();
        Registrable.requireLength("Employee number", employeeNumber, Identifiable.MIN_ID_LENGTH, Identifiable.MAX_ID_LENGTH);
        Registrable.requireLength("Specialization", specialization, 2, 120);
        Registrable.requireNotFuture("Hire date", hireDate);
        Registrable.requirePositive("Salary", salary);
        Registrable.requireLength("Office number", officeNumber, 1, 20);
        Registrable.requireOneOf("Employment type", employmentType, new String[]{"FULL_TIME", "PART_TIME", "CONTRACT"});
    }

    @Override
    public void printDetails() {
        super.printDetails();
        System.out.println("Employee no.  : " + employeeNumber);
        System.out.println("Specialization: " + specialization);
        System.out.println("Hire date     : " + hireDate);
        System.out.println("Salary        : " + salary);
        System.out.println("Office number : " + officeNumber);
        System.out.println("Employment    : " + employmentType);
    }

    @Override
    public String getRegistrationSummary() {
        return "Teacher [" + employeeNumber + "] " + getDisplayName() + " - " + specialization;
    }

    @Override
    public String toString() {
        return "Teacher [employeeNumber=" + employeeNumber + ", name=" + getDisplayName()
                + ", specialization=" + specialization + ", hireDate=" + hireDate + ", salary=" + salary
                + ", officeNumber=" + officeNumber + ", employmentType=" + employmentType + "]";
    }
}