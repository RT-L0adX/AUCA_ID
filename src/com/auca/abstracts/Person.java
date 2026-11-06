package com.auca.abstracts;

import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import java.time.LocalDate;

public abstract class Person implements Identifiable, Registrable {
    private String personId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;

    public Person(String personId, String firstName, String lastName,
                  String email, String phoneNumber, LocalDate dateOfBirth) {
        this.personId = Registrable.clean(personId);
        this.firstName = Registrable.clean(firstName);
        this.lastName = Registrable.clean(lastName);
        this.email = Registrable.clean(email);
        this.phoneNumber = Registrable.clean(phoneNumber);
        this.dateOfBirth = dateOfBirth;
    }

    public String getPersonId() { return personId; }
    public void setPersonId(String personId) { this.personId = Registrable.clean(personId); }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = Registrable.clean(firstName); }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = Registrable.clean(lastName); }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = Registrable.clean(email); }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = Registrable.clean(phoneNumber); }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public abstract String getRoleDescription();

    @Override
    public String getId() {
        return personId;
    }

    @Override
    public String getDisplayName() {
        return firstName + " " + lastName;
    }

    // isActive() is left abstract here: each subclass decides what "active" means

    @Override
    public void validate() throws ValidationException {
        Registrable.requireLength("Person ID", personId, Identifiable.MIN_ID_LENGTH, Identifiable.MAX_ID_LENGTH);
        Registrable.requireLength("First name", firstName, Registrable.MIN_NAME_LENGTH, Registrable.MAX_NAME_LENGTH);
        Registrable.requireLength("Last name", lastName, Registrable.MIN_NAME_LENGTH, Registrable.MAX_NAME_LENGTH);
        Registrable.requireEmail("Email", email);
        Registrable.requirePhone("Phone", phoneNumber);
        Registrable.requirePast("Date of birth", dateOfBirth);
    }

    @Override
    public void printDetails() {
        System.out.println("Person ID     : " + personId);
        System.out.println("Name          : " + getDisplayName());
        System.out.println("Email         : " + email);
        System.out.println("Phone         : " + phoneNumber);
        System.out.println("Date of birth : " + dateOfBirth);
    }

    @Override
    public String getRegistrationSummary() {
        return getRoleDescription() + " [" + personId + "] " + getDisplayName();
    }

    @Override
    public String toString() {
        return "Person [personId=" + personId + ", name=" + getDisplayName() + ", email=" + email
                + ", phoneNumber=" + phoneNumber + ", dateOfBirth=" + dateOfBirth + "]";
    }
}