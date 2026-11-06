package com.auca.feepayment;

import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import com.auca.student.Student;
import java.math.BigDecimal;
import java.time.LocalDate;

public class FeePayment implements Identifiable, Registrable {
    private String paymentId;
    private Student student;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String paymentMethod;        
    private String receiptNumber;

    public FeePayment(String paymentId, Student student, BigDecimal amount,
                      LocalDate paymentDate, String paymentMethod, String receiptNumber) {
        this.paymentId = Registrable.clean(paymentId);
        this.student = student;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentMethod = Registrable.clean(paymentMethod);
        this.receiptNumber = Registrable.clean(receiptNumber);
    }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = Registrable.clean(paymentId); }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = Registrable.clean(paymentMethod); }

    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = Registrable.clean(receiptNumber); }

    @Override
    public String getId() {
        return paymentId;
    }

    @Override
    public String getDisplayName() {
        return "Receipt " + receiptNumber + " - " + student.getDisplayName();
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void validate() throws ValidationException {
        Registrable.requireLength("Payment ID", paymentId, Identifiable.MIN_ID_LENGTH, Identifiable.MAX_ID_LENGTH);
        Registrable.requireNotNull("Student", student);
        Registrable.requirePositive("Amount", amount);
        Registrable.requireNotFuture("Payment date", paymentDate);
        Registrable.requireOneOf("Payment method", paymentMethod,
                new String[]{"CASH", "MOBILE_MONEY", "CARD", "BANK_TRANSFER"});
        Registrable.requireLength("Receipt number", receiptNumber, Identifiable.MIN_ID_LENGTH, Identifiable.MAX_ID_LENGTH);
    }

    @Override
    public void printDetails() {
        System.out.println("Payment ID    : " + paymentId);
        System.out.println("Student       : " + (student == null ? "none" : student.getDisplayName()));
        System.out.println("Amount        : " + amount);
        System.out.println("Date          : " + paymentDate);
        System.out.println("Method        : " + paymentMethod);
        System.out.println("Receipt number: " + receiptNumber);
    }

    @Override
    public String getRegistrationSummary() {
        return "[" + paymentId + "] " + amount + " by " + student.getDisplayName()
                + " on " + paymentDate + " (" + paymentMethod + ", receipt " + receiptNumber + ")";
    }

    @Override
    public String toString() {
        return "FeePayment [paymentId=" + paymentId + ", student=" + student.getDisplayName()
                + ", amount=" + amount + ", paymentDate=" + paymentDate
                + ", paymentMethod=" + paymentMethod + ", receiptNumber=" + receiptNumber + "]";
    }
}