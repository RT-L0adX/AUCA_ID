package com.auca.assessment;

import com.auca.enrollment.Enrollment;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import java.time.LocalDate;

public class Assessment implements Identifiable, Registrable {
    private String assessmentId;
    private Enrollment enrollment;
    private String assessmentType;       
    private double maximumScore;
    private double obtainedScore;
    private LocalDate assessmentDate;

    public Assessment(String assessmentId, Enrollment enrollment, String assessmentType,
                      double maximumScore, double obtainedScore, LocalDate assessmentDate) {
        this.assessmentId = Registrable.clean(assessmentId);
        this.enrollment = enrollment;
        this.assessmentType = Registrable.clean(assessmentType);
        this.maximumScore = maximumScore;
        this.obtainedScore = obtainedScore;
        this.assessmentDate = assessmentDate;
    }

    public String getAssessmentId() { return assessmentId; }
    public void setAssessmentId(String assessmentId) { this.assessmentId = Registrable.clean(assessmentId); }

    public Enrollment getEnrollment() { return enrollment; }
    public void setEnrollment(Enrollment enrollment) { this.enrollment = enrollment; }

    public String getAssessmentType() { return assessmentType; }
    public void setAssessmentType(String assessmentType) { this.assessmentType = Registrable.clean(assessmentType); }

    public double getMaximumScore() { return maximumScore; }
    public void setMaximumScore(double maximumScore) { this.maximumScore = maximumScore; }

    public double getObtainedScore() { return obtainedScore; }
    public void setObtainedScore(double obtainedScore) { this.obtainedScore = obtainedScore; }

    public LocalDate getAssessmentDate() { return assessmentDate; }
    public void setAssessmentDate(LocalDate assessmentDate) { this.assessmentDate = assessmentDate; }

    @Override
    public String getId() {
        return assessmentId;
    }

    @Override
    public String getDisplayName() {
        return assessmentType + " - " + enrollment.getStudent().getDisplayName();
    }

    @Override
    public boolean isActive() {
        return enrollment != null && enrollment.isActive();
    }

    @Override
    public void validate() throws ValidationException {
        Registrable.requireLength("Assessment ID", assessmentId, Identifiable.MIN_ID_LENGTH, Identifiable.MAX_ID_LENGTH);
        Registrable.requireNotNull("Enrollment", enrollment);
        if (!enrollment.isActive()) {
            throw new ValidationException("Only an active enrollment can be assessed.");
        }
        Registrable.requireOneOf("Assessment type", assessmentType,
                new String[]{"QUIZ", "ASSIGNMENT", "MIDTERM", "FINAL"});
        if (maximumScore <= 0) {
            throw new ValidationException("Maximum score must be greater than 0.");
        }
        if (obtainedScore < 0 || obtainedScore > maximumScore) {
            throw new ValidationException("Obtained score must be between 0 and " + maximumScore + ".");
        }
        Registrable.requireNotFuture("Assessment date", assessmentDate);
    }

    @Override
    public void printDetails() {
        System.out.println("Assessment ID : " + assessmentId);
        System.out.println("Enrollment    : " + (enrollment == null ? "none" : enrollment.getDisplayName()));
        System.out.println("Type          : " + assessmentType);
        System.out.println("Maximum score : " + maximumScore);
        System.out.println("Obtained score: " + obtainedScore);
        System.out.println("Date          : " + assessmentDate);
    }

    @Override
    public String getRegistrationSummary() {
        return "[" + assessmentId + "] " + assessmentType + " " + obtainedScore + "/" + maximumScore
                + " - " + enrollment.getStudent().getDisplayName() + " (" + enrollment.getCourse().getCourseCode() + ")";
    }

    @Override
    public String toString() {
        return "Assessment [assessmentId=" + assessmentId + ", enrollment=" + enrollment.getId()
                + ", assessmentType=" + assessmentType + ", maximumScore=" + maximumScore
                + ", obtainedScore=" + obtainedScore + ", assessmentDate=" + assessmentDate + "]";
    }
}