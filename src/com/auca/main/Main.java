package com.auca.main;

import com.auca.abstracts.AcademicMember;
import com.auca.abstracts.Person;
import com.auca.administrator.Administrator;
import com.auca.assessment.Assessment;
import com.auca.bookloan.BookLoan;
import com.auca.classroom.Classroom;
import com.auca.course.Course;
import com.auca.department.Department;
import com.auca.enrollment.Enrollment;
import com.auca.exceptions.BookUnavailableException;
import com.auca.exceptions.DuplicateRecordException;
import com.auca.exceptions.PrerequisiteMissingException;
import com.auca.exceptions.ValidationException;
import com.auca.feepayment.FeePayment;
import com.auca.interfaces.Registrable;
import com.auca.librarybook.LibraryBook;
import com.auca.student.Student;
import com.auca.teacher.Teacher;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import com.auca.exceptions.RecordNotFoundException;
import com.auca.interfaces.Identifiable;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class Main {
    // ONE shared Scanner for the whole application
    private static final Scanner SC = new Scanner(System.in);

    // Used only while a record is being UPDATED
    private static boolean editMode = false;
    private static Object editTarget = null;

    // ---------- Typed in-memory collections ----------
    private static final ArrayList<Department> departments = new ArrayList<>();
    private static final ArrayList<Student> students = new ArrayList<>();
    private static final ArrayList<Teacher> teachers = new ArrayList<>();
    private static final ArrayList<Administrator> administrators = new ArrayList<>();
    private static final ArrayList<Course> courses = new ArrayList<>();
    private static final ArrayList<Classroom> classrooms = new ArrayList<>();
    private static final ArrayList<Enrollment> enrollments = new ArrayList<>();
    private static final ArrayList<Assessment> assessments = new ArrayList<>();
    private static final ArrayList<FeePayment> payments = new ArrayList<>();
    private static final ArrayList<LibraryBook> books = new ArrayList<>();
    private static final ArrayList<BookLoan> loans = new ArrayList<>();

    // ---------- Field kinds used by the smart form engine ----------
    private static final int K_TEXT = 0;            // text with min/max length (+ optional uniqueness domain)
    private static final int K_EMAIL = 1;
    private static final int K_PHONE = 2;
    private static final int K_DATE_PAST = 3;       // must be before today
    private static final int K_DATE_NOT_FUTURE = 4; // today or earlier
    private static final int K_DATE_ANY = 5;
    private static final int K_DATE_AFTER = 6;      // must be after another date field
    private static final int K_INT = 7;             // whole number in a range
    private static final int K_INT_UPTO = 8;        // whole number from min up to another field's value
    private static final int K_MONEY = 9;           // BigDecimal > 0
    private static final int K_DOUBLE_POS = 10;     // double > 0
    private static final int K_SCORE_MAX = 11;      // double between 0 and another field's value
    private static final int K_CHOICE = 12;         // numbered choice from fixed options
    private static final int K_SELECT = 13;         // choose an existing object from a list
    private static final int K_FIXED = 14;          // derived value, not asked
    private static final int K_ISBN = 15;

    // Results of asking one field
    private static final int GO_NEXT = 0;
    private static final int GO_BACK = 1;
    private static final int GO_CANCEL = 2;

    // =====================================================================
    //  INPUT HELPERS
    // =====================================================================
    private static String ask(String prompt) {
        System.out.print(prompt);
        try {
            return SC.nextLine().trim();
        } catch (NoSuchElementException e) {
            System.out.println("\nInput closed. Exiting.");
            System.exit(0);
            return null;
        }
    }

    private static String str(Object[] v, int i) {
        return (String) v[i];
    }

    private static LocalDate date(Object[] v, int i) {
        return (LocalDate) v[i];
    }

    // =====================================================================
    //  FORM SPECIFICATIONS  (label, hint, kind, extra, min, max)
    // =====================================================================
    private static Object[] row(String label, String hint, int kind, Object extra, int min, int max) {
        return new Object[]{label, hint, kind, extra, min, max};
    }

    private static Object[][] join(Object[][] a, Object[][] b) {
        Object[][] result = new Object[a.length + b.length][];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i];
        }
        for (int i = 0; i < b.length; i++) {
            result[a.length + i] = b[i];
        }
        return result;
    }

    private static Object[][] personRows() {
        return new Object[][]{
            row("Person ID", "String | unique | 4-30 chars", K_TEXT, "PERSON_ID", 4, 30),
            row("First name", "String | 2-60 chars", K_TEXT, null, 2, 60),
            row("Last name", "String | 2-60 chars", K_TEXT, null, 2, 60),
            row("Email", "String | unique | email", K_EMAIL, "PERSON_EMAIL", 0, 0),
            row("Phone", "String | 10-15 digits, optional +", K_PHONE, null, 0, 0),
            row("Date of birth", "LocalDate | YYYY-MM-DD", K_DATE_PAST, null, 0, 0)
        };
    }

    private static Object[][] academicRows() {
        return new Object[][]{
            row("Member number", "String | unique | 4-30 chars", K_TEXT, "MEMBER_NO", 4, 30),
            row("Department", "Department | select", K_SELECT, "DEPARTMENT", 0, 0),
            row("Join date", "LocalDate | YYYY-MM-DD", K_DATE_NOT_FUTURE, null, 0, 0),
            row("Academic status", "", K_CHOICE, new String[]{"ACTIVE", "INACTIVE"}, 0, 0),
            row("Institutional email", "String | unique | email", K_EMAIL, "INST_EMAIL", 0, 0),
            row("Advisor name", "String | 2-120 chars", K_TEXT, null, 2, 120)
        };
    }

    private static Object[][] specFor(String type) {
        switch (type) {
            case "STUDENT":
                return join(join(personRows(), academicRows()), new Object[][]{
                    row("Student number", "String | unique | 4-30 chars", K_TEXT, "STUDENT_NO", 4, 30),
                    row("Program name", "String | 2-120 chars", K_TEXT, null, 2, 120),
                    row("Admission date", "LocalDate | YYYY-MM-DD", K_DATE_NOT_FUTURE, null, 0, 0),
                    row("Study year", "int | 1-6", K_INT, null, 1, 6),
                    row("Guardian phone", "String | 10-15 digits", K_PHONE, null, 0, 0),
                    row("Scholarship", "", K_CHOICE, new String[]{"YES", "NO"}, 0, 0)
                });
            case "TEACHER":
                return join(join(personRows(), academicRows()), new Object[][]{
                    row("Employee number", "String | unique | 4-30 chars", K_TEXT, "EMPLOYEE_NO", 4, 30),
                    row("Specialization", "String | 2-120 chars", K_TEXT, null, 2, 120),
                    row("Hire date", "LocalDate | YYYY-MM-DD", K_DATE_NOT_FUTURE, null, 0, 0),
                    row("Salary", "BigDecimal | greater than 0", K_MONEY, null, 0, 0),
                    row("Office number", "String | 1-20 chars", K_TEXT, null, 1, 20),
                    row("Employment type", "", K_CHOICE, new String[]{"FULL_TIME", "PART_TIME", "CONTRACT"}, 0, 0)
                });
            case "ADMINISTRATOR":
                return join(personRows(), new Object[][]{
                    row("Staff number", "String | unique | 4-30 chars", K_TEXT, "STAFF_NO", 4, 30),
                    row("Job title", "String | 2-120 chars", K_TEXT, null, 2, 120),
                    row("Assigned office", "String | 2-60 chars", K_TEXT, null, 2, 60),
                    row("Employment date", "LocalDate | YYYY-MM-DD", K_DATE_NOT_FUTURE, null, 0, 0),
                    row("Access level", "int | 1-5", K_INT, null, 1, 5),
                    row("Active", "", K_CHOICE, new String[]{"YES", "NO"}, 0, 0)
                });
            case "DEPARTMENT":
                return new Object[][]{
                    row("Department ID", "String | unique | 4-30 chars", K_TEXT, "DEPT_ID", 4, 30),
                    row("Department name", "String | 2-60 chars", K_TEXT, null, 2, 60),
                    row("Department code", "String | unique | 2-10 chars", K_TEXT, "DEPT_CODE", 2, 10),
                    row("Office location", "String | 2-60 chars", K_TEXT, null, 2, 60),
                    row("Contact email", "String | email", K_EMAIL, null, 0, 0),
                    row("Head teacher", "Teacher | optional", K_SELECT, "TEACHER", 1, 0)
                };
            case "COURSE":
                return new Object[][]{
                    row("Course ID", "String | unique | 4-30 chars", K_TEXT, "COURSE_ID", 4, 30),
                    row("Course code", "String | unique | 2-20 chars", K_TEXT, "COURSE_CODE", 2, 20),
                    row("Course title", "String | 2-120 chars", K_TEXT, null, 2, 120),
                    row("Credit hours", "int | 1-6", K_INT, null, 1, 6),
                    row("Department", "Department | select", K_SELECT, "DEPARTMENT", 0, 0),
                    row("Assigned teacher", "Teacher | select", K_SELECT, "TEACHER", 0, 0)
                };
            case "CLASSROOM":
                return new Object[][]{
                    row("Classroom ID", "String | unique | 4-30 chars", K_TEXT, "CLASSROOM_ID", 4, 30),
                    row("Building name", "String | 2-60 chars", K_TEXT, null, 2, 60),
                    row("Room number", "String | 1-20 chars", K_TEXT, null, 1, 20),
                    row("Capacity", "int | greater than 0", K_INT, null, 1, 5000),
                    row("Floor number", "int | 0 or more", K_INT, null, 0, 100),
                    row("Available", "", K_CHOICE, new String[]{"YES", "NO"}, 0, 0)
                };
            case "ENROLLMENT":
                return new Object[][]{
                    row("Enrollment ID", "String | unique | 4-30 chars", K_TEXT, "ENROLLMENT_ID", 4, 30),
                    row("Student", "Student | select", K_SELECT, "STUDENT", 0, 0),
                    row("Course", "Course | select", K_SELECT, "COURSE", 0, 0),
                    row("Enrollment date", "LocalDate | YYYY-MM-DD", K_DATE_NOT_FUTURE, null, 0, 0),
                    row("Semester", "", K_CHOICE, new String[]{"FALL", "SPRING", "SUMMER"}, 0, 0),
                    row("Enrollment status", "", K_CHOICE, new String[]{"ACTIVE", "COMPLETED", "DROPPED"}, 0, 0)
                };
            case "ASSESSMENT":
                return new Object[][]{
                    row("Assessment ID", "String | unique | 4-30 chars", K_TEXT, "ASSESSMENT_ID", 4, 30),
                    row("Enrollment", "active Enrollment | select", K_SELECT, "ENROLLMENT_ACTIVE", 0, 0),
                    row("Assessment type", "", K_CHOICE, new String[]{"QUIZ", "ASSIGNMENT", "MIDTERM", "FINAL"}, 0, 0),
                    row("Maximum score", "double | greater than 0", K_DOUBLE_POS, null, 0, 0),
                    row("Obtained score", "double | 0 to maximum score", K_SCORE_MAX, 3, 0, 0),
                    row("Assessment date", "LocalDate | YYYY-MM-DD", K_DATE_NOT_FUTURE, null, 0, 0)
                };
            case "FEEPAYMENT":
                return new Object[][]{
                    row("Payment ID", "String | unique | 4-30 chars", K_TEXT, "PAYMENT_ID", 4, 30),
                    row("Student", "Student | select", K_SELECT, "STUDENT", 0, 0),
                    row("Amount", "BigDecimal | greater than 0", K_MONEY, null, 0, 0),
                    row("Payment date", "LocalDate | YYYY-MM-DD", K_DATE_NOT_FUTURE, null, 0, 0),
                    row("Payment method", "", K_CHOICE, new String[]{"CASH", "MOBILE_MONEY", "CARD", "BANK_TRANSFER"}, 0, 0),
                    row("Receipt number", "String | unique | 4-30 chars", K_TEXT, "RECEIPT_NO", 4, 30)
                };
            case "BOOK":
                return new Object[][]{
                    row("Book ID", "String | unique | 4-30 chars", K_TEXT, "BOOK_ID", 4, 30),
                    row("ISBN", "String | unique | 10 or 13 digits", K_ISBN, "ISBN", 0, 0),
                    row("Title", "String | 2-120 chars", K_TEXT, null, 2, 120),
                    row("Author", "String | 2-120 chars", K_TEXT, null, 2, 120),
                    row("Total copies", "int | 0 or more", K_INT, null, 0, 100000),
                    row("Available copies", "int | 0 to total copies", K_INT_UPTO, 4, 0, 0)
                };
            case "BOOKLOAN":
                return new Object[][]{
                    row("Loan ID", "String | unique | 4-30 chars", K_TEXT, "LOAN_ID", 4, 30),
                    row("Student", "Student | select", K_SELECT, "STUDENT", 0, 0),
                    row("Library book", "LibraryBook | select (available)", K_SELECT, "BOOK_AVAILABLE", 0, 0),
                    row("Borrowed date", "LocalDate | YYYY-MM-DD", K_DATE_NOT_FUTURE, null, 0, 0),
                    row("Due date", "LocalDate | after borrowed date", K_DATE_AFTER, 3, 0, 0),
                    row("Returned date", "not returned yet (set when the book is returned)", K_FIXED, null, 0, 0)
                };
            default:
                throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    // First field index of each step (the review is always the last step)
    private static int[] stepStartsFor(String type) {
        switch (type) {
            case "STUDENT":
            case "TEACHER":
                return new int[]{0, 6, 12};
            case "ADMINISTRATOR":
                return new int[]{0, 6, 9};
            default:
                return new int[]{0, 2, 4};
        }
    }

    private static String[] stepNamesFor(String type) {
        switch (type) {
            case "STUDENT":
                return new String[]{"PERSONAL DETAILS", "ACADEMIC DETAILS", "STUDENT DETAILS"};
            case "TEACHER":
                return new String[]{"PERSONAL DETAILS", "ACADEMIC DETAILS", "TEACHER DETAILS"};
            case "ADMINISTRATOR":
                return new String[]{"PERSONAL DETAILS", "STAFF DETAILS", "EMPLOYMENT DETAILS"};
            default:
                return new String[]{"BASIC DETAILS", "MAIN DETAILS", "FINAL DETAILS"};
        }
    }

    private static String titleFor(String type) {
        switch (type) {
            case "DEPARTMENT": return "REGISTER DEPARTMENT";
            case "STUDENT": return "STUDENT REGISTRATION";
            case "TEACHER": return "TEACHER REGISTRATION";
            case "ADMINISTRATOR": return "ADMINISTRATOR REGISTRATION";
            case "COURSE": return "REGISTER COURSE";
            case "CLASSROOM": return "REGISTER CLASSROOM";
            case "ENROLLMENT": return "ENROLL STUDENT";
            case "ASSESSMENT": return "RECORD ASSESSMENT";
            case "FEEPAYMENT": return "RECORD FEE PAYMENT";
            case "BOOK": return "REGISTER LIBRARY BOOK";
            case "BOOKLOAN": return "BORROW LIBRARY BOOK";
            default: return type;
        }
    }

    private static String labelFor(String type) {
        switch (type) {
            case "DEPARTMENT": return "Department";
            case "STUDENT": return "Student";
            case "TEACHER": return "Teacher";
            case "ADMINISTRATOR": return "Administrator";
            case "COURSE": return "Course";
            case "CLASSROOM": return "Classroom";
            case "ENROLLMENT": return "Enrollment";
            case "ASSESSMENT": return "Assessment";
            case "FEEPAYMENT": return "Fee payment";
            case "BOOK": return "Library book";
            case "BOOKLOAN": return "Book loan";
            default: return type;
        }
    }

    // =====================================================================
    //  SELECTION LISTS (existing objects)
    // =====================================================================
    private static ArrayList<Registrable> optionsFor(String code) {
        ArrayList<Registrable> list = new ArrayList<>();
        switch (code) {
            case "DEPARTMENT":
                list.addAll(departments);
                break;
            case "TEACHER":
                list.addAll(teachers);
                break;
            case "STUDENT":
                list.addAll(students);
                break;
            case "COURSE":
                list.addAll(courses);
                break;
            case "ENROLLMENT_ACTIVE":
                for (Enrollment e : enrollments) {
                    if (e.isActive()) {
                        list.add(e);
                    }
                }
                break;
            case "BOOK_AVAILABLE":
                for (LibraryBook b : books) {
                    if (b.getAvailableCopies() > 0) {
                        list.add(b);
                    }
                }
                break;
            default:
                break;
        }
        return list;
    }

    // type to register when the user chooses "[+] Register a new ..."
    private static String typeForCode(String code) {
        switch (code) {
            case "ENROLLMENT_ACTIVE": return "ENROLLMENT";
            case "BOOK_AVAILABLE": return "BOOK";
            default: return code;
        }
    }

    private static String emptyMessage(String code) {
        switch (code) {
            case "DEPARTMENT": return "No Department exists. Register a Department first.";
            case "TEACHER": return "No Teacher exists. Register a Teacher first.";
            case "STUDENT": return "No Student exists. Register a Student first.";
            case "COURSE": return "No Course exists. Register a Course first.";
            case "ENROLLMENT_ACTIVE":
                return enrollments.isEmpty() ? "No Enrollment exists. Enroll a student first."
                        : "No active Enrollment exists. Enroll a student first.";
            case "BOOK_AVAILABLE":
                return books.isEmpty() ? "No Library book exists. Register a book first."
                        : "No library book has an available copy. All copies are on loan.";
            default: return "Nothing to select.";
        }
    }

    // Select an existing object for field "index". Offers nested registration.
    private static int selectField(Object[][] spec, int index, Object[] values, String title) {
        Object[] row = spec[index];
        String label = (String) row[0];
        String code = (String) row[3];
        boolean optional = ((int) row[4]) == 1;

        while (true) {
            ArrayList<Registrable> options = optionsFor(code);
            System.out.println();
            if (options.isEmpty()) {
                if (optional) {
                    System.out.println("No " + label + " exists yet. It can be assigned later.");
                    values[index] = null;
                    return GO_NEXT;
                }
                System.out.println(emptyMessage(code));
                System.out.println("  1 Register " + labelFor(typeForCode(code)) + " now");
                System.out.println("  0 Cancel");
                String answer = ask("Select option: ");
                if (answer.equalsIgnoreCase("BACK")) {
                    return GO_BACK;
                }
                if (answer.equals("0") || answer.equalsIgnoreCase("CANCEL")) {
                    throw new PrerequisiteMissingException(emptyMessage(code));
                }
                if (answer.equals("1")) {
                    registerEntity(typeForCode(code));
                    System.out.println("Returning to " + title + "...");
                } else {
                    System.out.println("! Invalid option. Choose 1 or 0.");
                }
                continue;
            }

            System.out.println("---- CHOOSE " + label.toUpperCase() + " ----");
            for (int i = 0; i < options.size(); i++) {
                System.out.println("  " + (i + 1) + " " + options.get(i).getRegistrationSummary());
            }
            int newOption = options.size() + 1;
            System.out.println("  " + newOption + " [+] Register a new " + labelFor(typeForCode(code)));
            System.out.println("  0 " + (optional ? "No " + label.toLowerCase() + " (assign later)" : "Cancel registration"));
            if (values[index] != null) {
                System.out.println("  (current: " + displayValue(values[index], row) + " - press ENTER to keep)");
            }
            String answer = ask("Select option: ");
            if (answer.equalsIgnoreCase("BACK")) {
                return GO_BACK;
            }
            if (answer.equalsIgnoreCase("CANCEL")) {
                return GO_CANCEL;
            }
            if (answer.isEmpty() && values[index] != null) {
                return GO_NEXT;
            }
            try {
                int n = Integer.parseInt(answer);
                if (n == 0) {
                    if (optional) {
                        values[index] = null;
                        return GO_NEXT;
                    }
                    return GO_CANCEL;
                }
                if (n == newOption) {
                    registerEntity(typeForCode(code));
                    System.out.println("Returning to " + title + "...");
                    continue;
                }
                if (n >= 1 && n <= options.size()) {
                    values[index] = options.get(n - 1);
                    return GO_NEXT;
                }
                System.out.println("! Choose a number from 0 to " + newOption + ".");
            } catch (NumberFormatException e) {
                System.out.println("! Invalid input. Enter the number of an option.");
            }
        }
    }

    // =====================================================================
    //  ONE FIELD: prompt, parse, validate, re-prompt only this field
    // =====================================================================
    private static String hintFor(Object[] row) {
        int kind = (int) row[2];
        if (kind == K_CHOICE) {
            String[] options = (String[]) row[3];
            String text = "";
            for (int i = 0; i < options.length; i++) {
                text += (i == 0 ? "" : " / ") + (i + 1) + " " + options[i];
            }
            return text;
        }
        return (String) row[1];
    }

    private static String displayValue(Object value, Object[] row) {
        if (value == null) {
            return ((int) row[2]) == K_FIXED ? "not returned yet" : "(none)";
        }
        if (value instanceof Registrable) {
            return ((Registrable) value).getRegistrationSummary();
        }
        return value.toString();
    }

    private static int askField(Object[][] spec, int index, Object[] values, String title) {
        Object[] row = spec[index];
        String label = (String) row[0];
        int kind = (int) row[2];

        if (kind == K_SELECT) {
            return selectField(spec, index, values, title);
        }
        if (kind == K_FIXED) {
            values[index] = null;
            System.out.println(String.format("%02d %-20s [derived: %s]", index + 1, label, row[1]));
            return GO_NEXT;
        }

        while (true) {
            String current = values[index] == null ? ""
                    : " (current: " + displayValue(values[index], row) + ", ENTER keeps)";
            String text = ask(String.format("%02d %-20s [%s]%s : ", index + 1, label, hintFor(row), current));
            if (text.equalsIgnoreCase("BACK")) {
                return GO_BACK;
            }
            if (text.equalsIgnoreCase("CANCEL")) {
                return GO_CANCEL;
            }
            if (text.isEmpty() && values[index] != null) {
                return GO_NEXT;
            }
            try {
                values[index] = parseField(row, text, values);
                return GO_NEXT;
            } catch (ValidationException | DuplicateRecordException e) {
                System.out.println("! " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("! Invalid number for " + label + ".");
            } catch (DateTimeParseException e) {
                System.out.println("! Invalid date: use YYYY-MM-DD (example: 2004-05-16).");
            }
            System.out.println("  Please re-enter field " + String.format("%02d", index + 1) + ".");
        }
    }

    // Converts the typed text to the right type and validates it
    private static Object parseField(Object[] row, String text, Object[] values) throws ValidationException {
        String label = (String) row[0];
        int kind = (int) row[2];
        Object extra = row[3];
        int min = (int) row[4];
        int max = (int) row[5];

        switch (kind) {
            case K_TEXT:
                Registrable.requireLength(label, text, min, max);
                requireUnique(label, (String) extra, text);
                return text;
            case K_EMAIL:
                Registrable.requireEmail(label, text);
                requireUnique(label, (String) extra, text);
                return text;
            case K_PHONE:
                Registrable.requirePhone(label, text);
                return text;
            case K_DATE_PAST: {
                LocalDate d = LocalDate.parse(text);
                Registrable.requirePast(label, d);
                return d;
            }
            case K_DATE_NOT_FUTURE: {
                LocalDate d = LocalDate.parse(text);
                Registrable.requireNotFuture(label, d);
                return d;
            }
            case K_DATE_ANY:
                return LocalDate.parse(text);
            case K_DATE_AFTER: {
                LocalDate d = LocalDate.parse(text);
                LocalDate reference = (LocalDate) values[(Integer) extra];
                if (reference == null) {
                    throw new ValidationException("Enter the borrowed date first.");
                }
                if (!d.isAfter(reference)) {
                    throw new ValidationException(label + " must be after " + reference + ".");
                }
                return d;
            }
            case K_INT: {
                int n = Integer.parseInt(text);
                Registrable.requireRange(label, n, min, max);
                return n;
            }
            case K_INT_UPTO: {
                int n = Integer.parseInt(text);
                Object limit = values[(Integer) extra];
                if (limit == null) {
                    throw new ValidationException("Enter the total copies first.");
                }
                Registrable.requireRange(label, n, min, (Integer) limit);
                return n;
            }
            case K_MONEY: {
                BigDecimal amount = new BigDecimal(text);
                Registrable.requirePositive(label, amount);
                return amount;
            }
            case K_DOUBLE_POS: {
                double x = Double.parseDouble(text);
                if (Double.isNaN(x) || Double.isInfinite(x) || x <= 0) {
                    throw new ValidationException(label + " must be a number greater than 0.");
                }
                return x;
            }
            case K_SCORE_MAX: {
                double x = Double.parseDouble(text);
                Object limit = values[(Integer) extra];
                if (limit == null) {
                    throw new ValidationException("Enter the maximum score first.");
                }
                if (Double.isNaN(x) || x < 0 || x > (Double) limit) {
                    throw new ValidationException(label + " must be between 0 and " + limit + ".");
                }
                return x;
            }
            case K_CHOICE: {
                String[] options = (String[]) extra;
                try {
                    int n = Integer.parseInt(text);
                    if (n >= 1 && n <= options.length) {
                        return options[n - 1];
                    }
                } catch (NumberFormatException e) {
                    for (int i = 0; i < options.length; i++) {
                        if (options[i].equalsIgnoreCase(text)) {
                            return options[i];
                        }
                    }
                }
                throw new ValidationException(label + " must be one of: " + hintFor(row) + ".");
            }
            case K_ISBN:
                if (!isValidIsbn(text)) {
                    throw new ValidationException("ISBN must contain 10 or 13 digits (hyphens allowed).");
                }
                requireUnique(label, (String) extra, text);
                return text;
            default:
                throw new ValidationException("Unsupported field.");
        }
    }

    private static boolean isValidIsbn(String value) {
        String digits = value.replace("-", "");
        if (digits.length() != 10 && digits.length() != 13) {
            return false;
        }
        for (int i = 0; i < digits.length(); i++) {
            if (!Character.isDigit(digits.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static void requireUnique(String label, String domain, String value) {
        if (domain != null && isTaken(domain, value, editTarget)) {
            throw new DuplicateRecordException(label + " '" + value + "' already exists.");
        }
    }

    // =====================================================================
    //  UNIQUENESS  (exclude = the record being edited, so it can keep its own value)
    // =====================================================================
    private static ArrayList<Person> allPeople() {
        ArrayList<Person> people = new ArrayList<>();
        people.addAll(students);
        people.addAll(teachers);
        people.addAll(administrators);
        return people;
    }

    private static ArrayList<AcademicMember> allAcademicMembers() {
        ArrayList<AcademicMember> members = new ArrayList<>();
        members.addAll(students);
        members.addAll(teachers);
        return members;
    }

    private static boolean sameText(String a, String b) {
        return a != null && a.equalsIgnoreCase(b.trim());
    }

    private static boolean isTaken(String domain, String value, Object exclude) {
        switch (domain) {
            case "PERSON_ID":
                for (Person p : allPeople()) {
                    if (p != exclude && sameText(p.getPersonId(), value)) return true;
                }
                return false;
            case "PERSON_EMAIL":
                for (Person p : allPeople()) {
                    if (p != exclude && sameText(p.getEmail(), value)) return true;
                }
                return false;
            case "MEMBER_NO":
                for (AcademicMember m : allAcademicMembers()) {
                    if (m != exclude && sameText(m.getMemberNumber(), value)) return true;
                }
                return false;
            case "INST_EMAIL":
                for (AcademicMember m : allAcademicMembers()) {
                    if (m != exclude && sameText(m.getInstitutionalEmail(), value)) return true;
                }
                return false;
            case "STUDENT_NO":
                for (Student s : students) {
                    if (s != exclude && sameText(s.getStudentNumber(), value)) return true;
                }
                return false;
            case "EMPLOYEE_NO":
                for (Teacher t : teachers) {
                    if (t != exclude && sameText(t.getEmployeeNumber(), value)) return true;
                }
                return false;
            case "STAFF_NO":
                for (Administrator a : administrators) {
                    if (a != exclude && sameText(a.getStaffNumber(), value)) return true;
                }
                return false;
            case "DEPT_ID":
                for (Department d : departments) {
                    if (d != exclude && sameText(d.getDepartmentId(), value)) return true;
                }
                return false;
            case "DEPT_CODE":
                for (Department d : departments) {
                    if (d != exclude && sameText(d.getDepartmentCode(), value)) return true;
                }
                return false;
            case "COURSE_ID":
                for (Course c : courses) {
                    if (c != exclude && sameText(c.getCourseId(), value)) return true;
                }
                return false;
            case "COURSE_CODE":
                for (Course c : courses) {
                    if (c != exclude && sameText(c.getCourseCode(), value)) return true;
                }
                return false;
            case "CLASSROOM_ID":
                for (Classroom c : classrooms) {
                    if (c != exclude && sameText(c.getClassroomId(), value)) return true;
                }
                return false;
            case "ENROLLMENT_ID":
                for (Enrollment e : enrollments) {
                    if (e != exclude && sameText(e.getEnrollmentId(), value)) return true;
                }
                return false;
            case "ASSESSMENT_ID":
                for (Assessment a : assessments) {
                    if (a != exclude && sameText(a.getAssessmentId(), value)) return true;
                }
                return false;
            case "PAYMENT_ID":
                for (FeePayment p : payments) {
                    if (p != exclude && sameText(p.getPaymentId(), value)) return true;
                }
                return false;
            case "RECEIPT_NO":
                for (FeePayment p : payments) {
                    if (p != exclude && sameText(p.getReceiptNumber(), value)) return true;
                }
                return false;
            case "BOOK_ID":
                for (LibraryBook b : books) {
                    if (b != exclude && sameText(b.getBookId(), value)) return true;
                }
                return false;
            case "ISBN":
                for (LibraryBook b : books) {
                    if (b != exclude && sameText(b.getIsbn(), value)) return true;
                }
                return false;
            case "LOAN_ID":
                for (BookLoan l : loans) {
                    if (l != exclude && sameText(l.getLoanId(), value)) return true;
                }
                return false;
            default:
                return false;
        }
    }

    // =====================================================================
    //  BUILD, CHECK AND COMMIT
    // =====================================================================
    private static Registrable build(String type, Object[] v) {
        switch (type) {
            case "STUDENT":
                return new Student(str(v, 0), str(v, 1), str(v, 2), str(v, 3), str(v, 4), date(v, 5),
                        str(v, 6), (Department) v[7], date(v, 8), str(v, 9), str(v, 10), str(v, 11),
                        str(v, 12), str(v, 13), date(v, 14), (Integer) v[15], str(v, 16), "YES".equals(v[17]));
            case "TEACHER":
                return new Teacher(str(v, 0), str(v, 1), str(v, 2), str(v, 3), str(v, 4), date(v, 5),
                        str(v, 6), (Department) v[7], date(v, 8), str(v, 9), str(v, 10), str(v, 11),
                        str(v, 12), str(v, 13), date(v, 14), (BigDecimal) v[15], str(v, 16), str(v, 17));
            case "ADMINISTRATOR":
                return new Administrator(str(v, 0), str(v, 1), str(v, 2), str(v, 3), str(v, 4), date(v, 5),
                        str(v, 6), str(v, 7), str(v, 8), date(v, 9), (Integer) v[10], "YES".equals(v[11]));
            case "DEPARTMENT":
                return new Department(str(v, 0), str(v, 1), str(v, 2), str(v, 3), str(v, 4), (Teacher) v[5]);
            case "COURSE":
                return new Course(str(v, 0), str(v, 1), str(v, 2), (Integer) v[3], (Department) v[4], (Teacher) v[5]);
            case "CLASSROOM":
                return new Classroom(str(v, 0), str(v, 1), str(v, 2), (Integer) v[3], (Integer) v[4], "YES".equals(v[5]));
            case "ENROLLMENT":
                return new Enrollment(str(v, 0), (Student) v[1], (Course) v[2], date(v, 3), str(v, 4), str(v, 5));
            case "ASSESSMENT":
                return new Assessment(str(v, 0), (Enrollment) v[1], str(v, 2), (Double) v[3], (Double) v[4], date(v, 5));
            case "FEEPAYMENT":
                return new FeePayment(str(v, 0), (Student) v[1], (BigDecimal) v[2], date(v, 3), str(v, 4), str(v, 5));
            case "BOOK":
                return new LibraryBook(str(v, 0), str(v, 1), str(v, 2), str(v, 3), (Integer) v[4], (Integer) v[5]);
            case "BOOKLOAN":
                return new BookLoan(str(v, 0), (Student) v[1], (LibraryBook) v[2], date(v, 3), date(v, 4), null);
            default:
                throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    // Uniqueness re-checked right before saving
    private static void recheckUniqueness(Object[][] spec, Object[] values) {
        for (int i = 0; i < spec.length; i++) {
            int kind = (int) spec[i][2];
            if ((kind == K_TEXT || kind == K_EMAIL || kind == K_ISBN) && spec[i][3] != null && values[i] != null) {
                requireUnique((String) spec[i][0], (String) spec[i][3], (String) values[i]);
            }
        }
    }

    // Cross-record business rules, checked before anything is committed
    private static void checkBusinessRules(String type, Registrable entity) {
        if (type.equals("ENROLLMENT")) {
            Enrollment n = (Enrollment) entity;
            for (Enrollment e : enrollments) {
                if (n.isActive() && e.isActive() && e.getStudent() == n.getStudent()
                        && e.getCourse() == n.getCourse() && e.getSemester().equals(n.getSemester())) {
                    throw new DuplicateRecordException("This student already has an active enrollment in "
                            + n.getCourse().getCourseCode() + " for " + n.getSemester() + ".");
                }
            }
        }
        if (type.equals("BOOKLOAN")) {
            BookLoan loan = (BookLoan) entity;
            if (loan.getBook().getAvailableCopies() <= 0) {
                throw new BookUnavailableException("No copy of '" + loan.getBook().getTitle() + "' is available.");
            }
        }
    }

    // Saves exactly one object. For a loan, stock goes down exactly once.
    private static void commit(String type, Registrable entity) {
        switch (type) {
            case "STUDENT": students.add((Student) entity); break;
            case "TEACHER": teachers.add((Teacher) entity); break;
            case "ADMINISTRATOR": administrators.add((Administrator) entity); break;
            case "DEPARTMENT": departments.add((Department) entity); break;
            case "COURSE": courses.add((Course) entity); break;
            case "CLASSROOM": classrooms.add((Classroom) entity); break;
            case "ENROLLMENT": enrollments.add((Enrollment) entity); break;
            case "ASSESSMENT": assessments.add((Assessment) entity); break;
            case "FEEPAYMENT": payments.add((FeePayment) entity); break;
            case "BOOK": books.add((LibraryBook) entity); break;
            case "BOOKLOAN":
                BookLoan loan = (BookLoan) entity;
                loan.getBook().setAvailableCopies(loan.getBook().getAvailableCopies() - 1);
                loans.add(loan);
                break;
            default:
                throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    // =====================================================================
    //  THE FORM: collect fields -> review -> save / edit / cancel
    // =====================================================================
    private static int stepOf(int index, int[] starts) {
        int step = 0;
        for (int i = 0; i < starts.length; i++) {
            if (index >= starts[i]) {
                step = i;
            }
        }
        return step;
    }

    // Returns the saved object, or null if the user cancelled. Nothing is saved on cancel.
    private static Registrable registerEntity(String type) {
        Object[][] spec = specFor(type);
        int[] starts = stepStartsFor(type);
        String[] names = stepNamesFor(type);
        String title = titleFor(type);
        Object[] values = new Object[spec.length];

        System.out.println("\n+------------------------------------------------------------+");
        System.out.println("| AUCA_ID / " + title);
        System.out.println("| Type BACK for the previous field, CANCEL to discard.       |");
        System.out.println("+------------------------------------------------------------+");

        try {
            int i = 0;
            int shownStep = -1;
            while (i < spec.length) {
                int step = stepOf(i, starts);
                if (step != shownStep) {
                    System.out.println("\n[STEP " + (step + 1) + "/" + (starts.length + 1) + "] " + names[step]);
                    shownStep = step;
                }
                int result = askField(spec, i, values, title);
                if (result == GO_NEXT) {
                    i++;
                } else if (result == GO_BACK) {
                    if (i > 0) {
                        i--;
                    } else {
                        System.out.println("This is the first field.");
                    }
                } else {
                    System.out.println("Registration cancelled. Nothing was saved.");
                    return null;
                }
            }
            return reviewAndSave(type, spec, values, starts, names, title);
        } catch (PrerequisiteMissingException e) {
            System.out.println("! " + e.getMessage());
            System.out.println("Registration cancelled. Nothing was saved.");
            return null;
        }
    }

    private static Registrable reviewAndSave(String type, Object[][] spec, Object[] values,
                                             int[] starts, String[] names, String title) {
        while (true) {
            System.out.println("\n+---------------- REVIEW: " + title + " ----------------");
            int shownStep = -1;
            for (int i = 0; i < spec.length; i++) {
                int step = stepOf(i, starts);
                if (step != shownStep) {
                    System.out.println("| " + names[step]);
                    shownStep = step;
                }
                System.out.println(String.format("|  %02d %-20s : %s", i + 1, spec[i][0], displayValue(values[i], spec[i])));
            }
            System.out.println("+---------------------------------------------------------");
            System.out.println("| [1] Save   [2] Edit field   [0] Cancel");
            String choice = ask("Select option: ");

            if (choice.equals("0") || choice.equalsIgnoreCase("CANCEL")) {
                System.out.println("Registration cancelled. Nothing was saved.");
                return null;
            }
            if (choice.equals("2")) {
                editOneField(spec, values, title);
                continue;
            }
            if (choice.equals("1")) {
                try {
                    Registrable entity = build(type, values);
                    entity.validate();
                    recheckUniqueness(spec, values);
                    checkBusinessRules(type, entity);
                    commit(type, entity);
                    System.out.println("+ " + labelFor(type) + " saved successfully: " + entity.getRegistrationSummary());
                    return entity;
                } catch (ValidationException | DuplicateRecordException | BookUnavailableException e) {
                    System.out.println("! Cannot save: " + e.getMessage());
                    System.out.println("  Use option 2 to correct the field, or 0 to cancel.");
                }
                continue;
            }
            System.out.println("! Invalid option. Choose 1, 2 or 0.");
        }
    }

    private static void editOneField(Object[][] spec, Object[] values, String title) {
        String text = ask("Field number to edit (01-" + String.format("%02d", spec.length) + "): ");
        try {
            int index = Integer.parseInt(text) - 1;
            if (index < 0 || index >= spec.length) {
                System.out.println("! There is no field " + text + ".");
                return;
            }
            if ((int) spec[index][2] == K_FIXED) {
                System.out.println("! This field is derived and cannot be edited here.");
                return;
            }
            System.out.println("Old value: " + displayValue(values[index], spec[index]));
            askField(spec, index, values, title);
        } catch (NumberFormatException e) {
            System.out.println("! Enter a field number.");
        }
    }

    // Menu entry point for a registration: offers "register another" after a save
    private static void registerFlow(String type) {
        while (true) {
            Registrable saved = registerEntity(type);
            if (saved == null) {
                return;
            }
            while (true) {
                System.out.println("\n[1] Register another   [2] View record   [0] Main menu");
                String choice = ask("Select option: ");
                if (choice.equals("1")) {
                    break;
                }
                if (choice.equals("2")) {
                    saved.printDetails();
                } else if (choice.equals("0")) {
                    return;
                } else {
                    System.out.println("! Invalid option.");
                }
            }
        }
    }

    // =====================================================================
    //  STEP 6 - LIST HELPERS
    // =====================================================================
    private static final String[] TYPES = {"DEPARTMENT", "STUDENT", "TEACHER", "ADMINISTRATOR", "COURSE",
        "CLASSROOM", "ENROLLMENT", "ASSESSMENT", "FEEPAYMENT", "BOOK", "BOOKLOAN"};

    private static ArrayList<Registrable> recordsOf(String type) {
        ArrayList<Registrable> list = new ArrayList<>();
        switch (type) {
            case "DEPARTMENT": list.addAll(departments); break;
            case "STUDENT": list.addAll(students); break;
            case "TEACHER": list.addAll(teachers); break;
            case "ADMINISTRATOR": list.addAll(administrators); break;
            case "COURSE": list.addAll(courses); break;
            case "CLASSROOM": list.addAll(classrooms); break;
            case "ENROLLMENT": list.addAll(enrollments); break;
            case "ASSESSMENT": list.addAll(assessments); break;
            case "FEEPAYMENT": list.addAll(payments); break;
            case "BOOK": list.addAll(books); break;
            case "BOOKLOAN": list.addAll(loans); break;
            default: break;
        }
        return list;
    }

    // Numbered list of records. Returns the chosen index, or -1 when the user selects 0.
    private static int chooseFrom(String heading, ArrayList<? extends Registrable> items) {
        while (true) {
            System.out.println("\n---- " + heading + " ----");
            for (int i = 0; i < items.size(); i++) {
                System.out.println("  " + (i + 1) + " " + items.get(i).getRegistrationSummary());
            }
            System.out.println("  0 Back / Cancel");
            String answer = ask("Select option: ");
            try {
                int n = Integer.parseInt(answer);
                if (n == 0) {
                    return -1;
                }
                if (n >= 1 && n <= items.size()) {
                    return n - 1;
                }
                System.out.println("! Choose a number from 0 to " + items.size() + ".");
            } catch (NumberFormatException e) {
                System.out.println("! Invalid input. Enter a number.");
            }
        }
    }

    // Choose one of the 11 record types. Returns its code, "ALL" (if allowed), or null for Back.
    private static String chooseType(String heading, boolean allowAll) {
        while (true) {
            System.out.println("\n---- " + heading + " ----");
            for (int i = 0; i < TYPES.length; i++) {
                System.out.println("  " + (i + 1) + " " + labelFor(TYPES[i]) + " (" + recordsOf(TYPES[i]).size() + ")");
            }
            if (allowAll) {
                System.out.println("  " + (TYPES.length + 1) + " All records");
            }
            System.out.println("  0 Back");
            String answer = ask("Select option: ");
            try {
                int n = Integer.parseInt(answer);
                if (n == 0) {
                    return null;
                }
                if (n >= 1 && n <= TYPES.length) {
                    return TYPES[n - 1];
                }
                if (allowAll && n == TYPES.length + 1) {
                    return "ALL";
                }
                System.out.println("! Option out of range.");
            } catch (NumberFormatException e) {
                System.out.println("! Invalid input. Enter a number.");
            }
        }
    }

    private static boolean confirm(String question) {
        return ask(question + " (1 Yes / 0 No): ").equals("1");
    }

    // =====================================================================
    //  13. VIEW ALL RECORDS
    // =====================================================================
    private static void viewAllRecords() {
        while (true) {
            String type = chooseType("VIEW ALL RECORDS - choose a record type", true);
            if (type == null) {
                return;
            }
            if (type.equals("ALL")) {
                int total = 0;
                for (int t = 0; t < TYPES.length; t++) {
                    ArrayList<Registrable> records = recordsOf(TYPES[t]);
                    System.out.println("\n== " + labelFor(TYPES[t]).toUpperCase() + " (" + records.size() + ") ==");
                    if (records.isEmpty()) {
                        System.out.println("  (no records)");
                    }
                    for (Registrable r : records) {
                        System.out.println("  " + r.getRegistrationSummary());
                    }
                    total += records.size();
                }
                System.out.println("\nTotal records: " + total);
                continue;
            }
            ArrayList<Registrable> records = recordsOf(type);
            if (records.isEmpty()) {
                System.out.println("\nNo " + labelFor(type) + " records have been registered yet.");
                continue;
            }
            while (true) {
                int index = chooseFrom(labelFor(type).toUpperCase() + " RECORDS (select a number for full details)", records);
                if (index < 0) {
                    break;
                }
                System.out.println();
                records.get(index).printDetails();
            }
        }
    }

    // =====================================================================
    //  14. SEARCH RECORD BY ID
    // =====================================================================
    private static boolean matchesId(Registrable record, String id) {
        if (((Identifiable) record).getId().equalsIgnoreCase(id)) {
            return true;
        }
        // people can also be found by their student / employee / staff number
        if (record instanceof Student && ((Student) record).getStudentNumber().equalsIgnoreCase(id)) {
            return true;
        }
        if (record instanceof Teacher && ((Teacher) record).getEmployeeNumber().equalsIgnoreCase(id)) {
            return true;
        }
        return record instanceof Administrator && ((Administrator) record).getStaffNumber().equalsIgnoreCase(id);
    }

    private static void searchById() {
        String id = ask("Enter the ID to search (or student/employee/staff number): ");
        if (id.isEmpty()) {
            System.out.println("! The ID cannot be blank.");
            return;
        }
        int found = 0;
        for (int t = 0; t < TYPES.length; t++) {
            for (Registrable record : recordsOf(TYPES[t])) {
                if (matchesId(record, id)) {
                    System.out.println("\n--- FOUND: " + labelFor(TYPES[t]).toUpperCase() + " ---");
                    record.printDetails();
                    found++;
                }
            }
        }
        if (found == 0) {
            throw new RecordNotFoundException("No record was found with ID '" + id + "'");
        }
    }

    // =====================================================================
    //  12. RETURN LIBRARY BOOK  (updates the existing loan, stock +1 exactly once)
    // =====================================================================
    private static void returnBook() {
        ArrayList<BookLoan> active = new ArrayList<>();
        for (BookLoan loan : loans) {
            if (loan.isActive()) {
                active.add(loan);
            }
        }
        if (active.isEmpty()) {
            throw new RecordNotFoundException("No active loan was found. Borrow a book first");
        }
        int index = chooseFrom("SELECT THE LOAN TO RETURN", active);
        if (index < 0) {
            System.out.println("Return cancelled. Nothing was changed.");
            return;
        }
        BookLoan loan = active.get(index);

        LocalDate returned = null;
        while (returned == null) {
            String text = ask("Returned date [LocalDate | YYYY-MM-DD | CANCEL to stop] : ");
            if (text.equalsIgnoreCase("CANCEL")) {
                System.out.println("Return cancelled. Nothing was changed.");
                return;
            }
            try {
                LocalDate d = LocalDate.parse(text);
                Registrable.requireNotFuture("Returned date", d);
                if (d.isBefore(loan.getBorrowedDate())) {
                    throw new ValidationException("Returned date cannot be before the borrowed date ("
                            + loan.getBorrowedDate() + ").");
                }
                returned = d;
            } catch (DateTimeParseException e) {
                System.out.println("! Invalid date: use YYYY-MM-DD (example: 2026-11-04).");
            } catch (ValidationException e) {
                System.out.println("! " + e.getMessage());
            }
        }

        System.out.println("\n--- RETURN SUMMARY ---");
        System.out.println("Book     : " + loan.getBook().getTitle());
        System.out.println("Student  : " + loan.getStudent().getDisplayName());
        System.out.println("Borrowed : " + loan.getBorrowedDate() + "   Due: " + loan.getDueDate());
        System.out.println("Returned : " + returned);
        if (returned.isAfter(loan.getDueDate())) {
            System.out.println("Note     : returned " + ChronoUnit.DAYS.between(loan.getDueDate(), returned) + " day(s) late.");
        }
        if (!confirm("Confirm the return?")) {
            System.out.println("Return cancelled. Nothing was changed.");
            return;
        }

        LibraryBook book = loan.getBook();
        if (book.getAvailableCopies() >= book.getTotalCopies()) {
            System.out.println("! Cannot return: available copies already equal total copies. Check the book record.");
            return;
        }
        loan.setReturnedDate(returned);
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        System.out.println("+ Book returned. '" + book.getTitle() + "' now has "
                + book.getAvailableCopies() + "/" + book.getTotalCopies() + " copies available.");
    }

    // =====================================================================
    //  15. UPDATE EXISTING RECORD
    // =====================================================================
    // Fields that cannot be changed because other records depend on them
    private static boolean isLocked(String type, int index) {
        if (!editMode) {
            return false;
        }
        switch (type) {
            case "ENROLLMENT": return index == 1 || index == 2;
            case "ASSESSMENT": return index == 1;
            case "FEEPAYMENT": return index == 1;
            case "BOOKLOAN": return index == 1 || index == 2 || index == 3 || index == 5;
            default: return false;
        }
    }

    private static void printReview(String type, String title, Object[][] spec, Object[] values,
                                    int[] starts, String[] names) {
        System.out.println("\n+---------------- REVIEW: " + title + " ----------------");
        int shownStep = -1;
        for (int i = 0; i < spec.length; i++) {
            int step = stepOf(i, starts);
            if (step != shownStep) {
                System.out.println("| " + names[step]);
                shownStep = step;
            }
            System.out.println(String.format("|  %02d %-20s : %s%s", i + 1, spec[i][0],
                    displayValue(values[i], spec[i]), isLocked(type, i) ? "   (locked)" : ""));
        }
        System.out.println("+---------------------------------------------------------");
    }

    // Copies the record's current values into the same layout the form uses
    private static Object[] valuesOf(String type, Registrable e) {
        switch (type) {
            case "STUDENT": {
                Student s = (Student) e;
                return new Object[]{s.getPersonId(), s.getFirstName(), s.getLastName(), s.getEmail(),
                    s.getPhoneNumber(), s.getDateOfBirth(), s.getMemberNumber(), s.getDepartment(),
                    s.getJoinDate(), s.getAcademicStatus(), s.getInstitutionalEmail(), s.getAdvisorName(),
                    s.getStudentNumber(), s.getProgramName(), s.getAdmissionDate(), s.getStudyYear(),
                    s.getGuardianPhone(), s.isScholarshipStatus() ? "YES" : "NO"};
            }
            case "TEACHER": {
                Teacher t = (Teacher) e;
                return new Object[]{t.getPersonId(), t.getFirstName(), t.getLastName(), t.getEmail(),
                    t.getPhoneNumber(), t.getDateOfBirth(), t.getMemberNumber(), t.getDepartment(),
                    t.getJoinDate(), t.getAcademicStatus(), t.getInstitutionalEmail(), t.getAdvisorName(),
                    t.getEmployeeNumber(), t.getSpecialization(), t.getHireDate(), t.getSalary(),
                    t.getOfficeNumber(), t.getEmploymentType()};
            }
            case "ADMINISTRATOR": {
                Administrator a = (Administrator) e;
                return new Object[]{a.getPersonId(), a.getFirstName(), a.getLastName(), a.getEmail(),
                    a.getPhoneNumber(), a.getDateOfBirth(), a.getStaffNumber(), a.getJobTitle(),
                    a.getAssignedOffice(), a.getEmploymentDate(), a.getAccessLevel(), a.isActive() ? "YES" : "NO"};
            }
            case "DEPARTMENT": {
                Department d = (Department) e;
                return new Object[]{d.getDepartmentId(), d.getDepartmentName(), d.getDepartmentCode(),
                    d.getOfficeLocation(), d.getContactEmail(), d.getHeadTeacher()};
            }
            case "COURSE": {
                Course c = (Course) e;
                return new Object[]{c.getCourseId(), c.getCourseCode(), c.getCourseTitle(), c.getCreditHours(),
                    c.getDepartment(), c.getAssignedTeacher()};
            }
            case "CLASSROOM": {
                Classroom c = (Classroom) e;
                return new Object[]{c.getClassroomId(), c.getBuildingName(), c.getRoomNumber(), c.getCapacity(),
                    c.getFloorNumber(), c.isAvailable() ? "YES" : "NO"};
            }
            case "ENROLLMENT": {
                Enrollment n = (Enrollment) e;
                return new Object[]{n.getEnrollmentId(), n.getStudent(), n.getCourse(), n.getEnrollmentDate(),
                    n.getSemester(), n.getEnrollmentStatus()};
            }
            case "ASSESSMENT": {
                Assessment a = (Assessment) e;
                return new Object[]{a.getAssessmentId(), a.getEnrollment(), a.getAssessmentType(),
                    a.getMaximumScore(), a.getObtainedScore(), a.getAssessmentDate()};
            }
            case "FEEPAYMENT": {
                FeePayment p = (FeePayment) e;
                return new Object[]{p.getPaymentId(), p.getStudent(), p.getAmount(), p.getPaymentDate(),
                    p.getPaymentMethod(), p.getReceiptNumber()};
            }
            case "BOOK": {
                LibraryBook b = (LibraryBook) e;
                return new Object[]{b.getBookId(), b.getIsbn(), b.getTitle(), b.getAuthor(),
                    b.getTotalCopies(), b.getAvailableCopies()};
            }
            case "BOOKLOAN": {
                BookLoan l = (BookLoan) e;
                return new Object[]{l.getLoanId(), l.getStudent(), l.getBook(), l.getBorrowedDate(),
                    l.getDueDate(), null};
            }
            default:
                throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    // Writes the edited values into the EXISTING object through its setters,
    // so other records that reference it keep pointing to the same object.
    private static void applyValues(String type, Registrable e, Object[] v) {
        switch (type) {
            case "STUDENT": {
                Student s = (Student) e;
                s.setPersonId(str(v, 0));
                s.setFirstName(str(v, 1));
                s.setLastName(str(v, 2));
                s.setEmail(str(v, 3));
                s.setPhoneNumber(str(v, 4));
                s.setDateOfBirth(date(v, 5));
                s.setMemberNumber(str(v, 6));
                s.setDepartment((Department) v[7]);
                s.setJoinDate(date(v, 8));
                s.setAcademicStatus(str(v, 9));
                s.setInstitutionalEmail(str(v, 10));
                s.setAdvisorName(str(v, 11));
                s.setStudentNumber(str(v, 12));
                s.setProgramName(str(v, 13));
                s.setAdmissionDate(date(v, 14));
                s.setStudyYear((Integer) v[15]);
                s.setGuardianPhone(str(v, 16));
                s.setScholarshipStatus("YES".equals(v[17]));
                break;
            }
            case "TEACHER": {
                Teacher t = (Teacher) e;
                t.setPersonId(str(v, 0));
                t.setFirstName(str(v, 1));
                t.setLastName(str(v, 2));
                t.setEmail(str(v, 3));
                t.setPhoneNumber(str(v, 4));
                t.setDateOfBirth(date(v, 5));
                t.setMemberNumber(str(v, 6));
                t.setDepartment((Department) v[7]);
                t.setJoinDate(date(v, 8));
                t.setAcademicStatus(str(v, 9));
                t.setInstitutionalEmail(str(v, 10));
                t.setAdvisorName(str(v, 11));
                t.setEmployeeNumber(str(v, 12));
                t.setSpecialization(str(v, 13));
                t.setHireDate(date(v, 14));
                t.setSalary((BigDecimal) v[15]);
                t.setOfficeNumber(str(v, 16));
                t.setEmploymentType(str(v, 17));
                break;
            }
            case "ADMINISTRATOR": {
                Administrator a = (Administrator) e;
                a.setPersonId(str(v, 0));
                a.setFirstName(str(v, 1));
                a.setLastName(str(v, 2));
                a.setEmail(str(v, 3));
                a.setPhoneNumber(str(v, 4));
                a.setDateOfBirth(date(v, 5));
                a.setStaffNumber(str(v, 6));
                a.setJobTitle(str(v, 7));
                a.setAssignedOffice(str(v, 8));
                a.setEmploymentDate(date(v, 9));
                a.setAccessLevel((Integer) v[10]);
                a.setActive("YES".equals(v[11]));
                break;
            }
            case "DEPARTMENT": {
                Department d = (Department) e;
                d.setDepartmentId(str(v, 0));
                d.setDepartmentName(str(v, 1));
                d.setDepartmentCode(str(v, 2));
                d.setOfficeLocation(str(v, 3));
                d.setContactEmail(str(v, 4));
                d.setHeadTeacher((Teacher) v[5]);
                break;
            }
            case "COURSE": {
                Course c = (Course) e;
                c.setCourseId(str(v, 0));
                c.setCourseCode(str(v, 1));
                c.setCourseTitle(str(v, 2));
                c.setCreditHours((Integer) v[3]);
                c.setDepartment((Department) v[4]);
                c.setAssignedTeacher((Teacher) v[5]);
                break;
            }
            case "CLASSROOM": {
                Classroom c = (Classroom) e;
                c.setClassroomId(str(v, 0));
                c.setBuildingName(str(v, 1));
                c.setRoomNumber(str(v, 2));
                c.setCapacity((Integer) v[3]);
                c.setFloorNumber((Integer) v[4]);
                c.setAvailable("YES".equals(v[5]));
                break;
            }
            case "ENROLLMENT": {
                Enrollment n = (Enrollment) e;
                n.setEnrollmentId(str(v, 0));
                n.setEnrollmentDate(date(v, 3));
                n.setSemester(str(v, 4));
                n.setEnrollmentStatus(str(v, 5));
                break;
            }
            case "ASSESSMENT": {
                Assessment a = (Assessment) e;
                a.setAssessmentId(str(v, 0));
                a.setAssessmentType(str(v, 2));
                a.setMaximumScore((Double) v[3]);
                a.setObtainedScore((Double) v[4]);
                a.setAssessmentDate(date(v, 5));
                break;
            }
            case "FEEPAYMENT": {
                FeePayment p = (FeePayment) e;
                p.setPaymentId(str(v, 0));
                p.setAmount((BigDecimal) v[2]);
                p.setPaymentDate(date(v, 3));
                p.setPaymentMethod(str(v, 4));
                p.setReceiptNumber(str(v, 5));
                break;
            }
            case "BOOK": {
                LibraryBook b = (LibraryBook) e;
                b.setBookId(str(v, 0));
                b.setIsbn(str(v, 1));
                b.setTitle(str(v, 2));
                b.setAuthor(str(v, 3));
                b.setTotalCopies((Integer) v[4]);
                b.setAvailableCopies((Integer) v[5]);
                break;
            }
            case "BOOKLOAN": {
                BookLoan l = (BookLoan) e;
                l.setLoanId(str(v, 0));
                l.setDueDate(date(v, 4));
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    // Same as the registration edit, but some fields are locked while updating
    private static void editUpdateField(String type, Object[][] spec, Object[] values, String title) {
        String text = ask("Field number to edit (01-" + String.format("%02d", spec.length) + "): ");
        try {
            int index = Integer.parseInt(text) - 1;
            if (index < 0 || index >= spec.length) {
                System.out.println("! There is no field " + text + ".");
                return;
            }
            if (isLocked(type, index)) {
                System.out.println("! This field cannot be edited (other records depend on it).");
                return;
            }
            if ((int) spec[index][2] == K_FIXED) {
                System.out.println("! This field is derived and cannot be edited here.");
                return;
            }
            System.out.println("Old value: " + displayValue(values[index], spec[index]));
            askField(spec, index, values, title);
        } catch (NumberFormatException e) {
            System.out.println("! Enter a field number.");
        }
    }

    // Business rule re-checked on update (the record being updated is ignored)
    private static void checkUpdateRules(String type, Registrable candidate, Object target) {
        if (type.equals("ENROLLMENT")) {
            Enrollment n = (Enrollment) candidate;
            for (Enrollment e : enrollments) {
                if (e != target && n.isActive() && e.isActive() && e.getStudent() == n.getStudent()
                        && e.getCourse() == n.getCourse() && e.getSemester().equals(n.getSemester())) {
                    throw new DuplicateRecordException("This student already has an active enrollment in "
                            + n.getCourse().getCourseCode() + " for " + n.getSemester() + ".");
                }
            }
        }
    }

    private static void updateRecord() {
        String type = chooseType("UPDATE RECORD - choose a record type", false);
        if (type == null) {
            return;
        }
        ArrayList<Registrable> records = recordsOf(type);
        if (records.isEmpty()) {
            throw new RecordNotFoundException("No " + labelFor(type) + " record exists to update");
        }
        int index = chooseFrom("SELECT THE " + labelFor(type).toUpperCase() + " TO UPDATE", records);
        if (index < 0) {
            return;
        }
        Registrable target = records.get(index);
        Object[][] spec = specFor(type);
        int[] starts = stepStartsFor(type);
        String[] names = stepNamesFor(type);
        String title = "UPDATE " + labelFor(type).toUpperCase();
        Object[] values = valuesOf(type, target);     // a COPY: the record changes only on Save

        editMode = true;
        editTarget = target;
        try {
            while (true) {
                printReview(type, title, spec, values, starts, names);
                System.out.println("| [1] Save changes   [2] Edit field   [0] Cancel");
                String choice = ask("Select option: ");
                if (choice.equals("0") || choice.equalsIgnoreCase("CANCEL")) {
                    System.out.println("Update cancelled. The record was not changed.");
                    return;
                }
                if (choice.equals("2")) {
                    editUpdateField(type, spec, values, title);
                    continue;
                }
                if (choice.equals("1")) {
                    if (!confirm("Save these changes?")) {
                        System.out.println("Nothing was saved.");
                        continue;
                    }
                    try {
                        Registrable candidate = build(type, values);   // check everything on a temporary copy first
                        candidate.validate();
                        recheckUniqueness(spec, values);
                        checkUpdateRules(type, candidate, target);
                        applyValues(type, target, values);             // only now is the real record changed
                        System.out.println("+ Record updated: " + target.getRegistrationSummary());
                        return;
                    } catch (ValidationException | DuplicateRecordException e) {
                        System.out.println("! Cannot save: " + e.getMessage());
                        System.out.println("  Use option 2 to correct the field, or 0 to cancel.");
                    }
                    continue;
                }
                System.out.println("! Invalid option. Choose 1, 2 or 0.");
            }
        } catch (PrerequisiteMissingException e) {
            System.out.println("! " + e.getMessage());
            System.out.println("Update cancelled. The record was not changed.");
        } finally {
            editMode = false;
            editTarget = null;
        }
    }

    // =====================================================================
    //  16, 17, 18 - REPORTS
    // =====================================================================
    private static Student chooseStudent(String heading) {
        if (students.isEmpty()) {
            throw new RecordNotFoundException("No student is registered yet");
        }
        int index = chooseFrom(heading, students);
        return index < 0 ? null : students.get(index);
    }

    private static void showAcademicSummary() {
        Student s = chooseStudent("SELECT A STUDENT");
        if (s == null) {
            return;
        }
        System.out.println("\n========== STUDENT ACADEMIC SUMMARY ==========");
        System.out.println("Student      : " + s.getDisplayName() + " [" + s.getStudentNumber() + "]");
        System.out.println("Program      : " + s.getProgramName() + " (year " + s.getStudyYear() + ")");
        System.out.println("Department   : " + s.getDepartment().getDisplayName());
        System.out.println("Advisor      : " + s.getAdvisorName());

        int enrollmentCount = 0;
        int activeCredits = 0;
        for (Enrollment e : enrollments) {
            if (e.getStudent() != s) {
                continue;
            }
            enrollmentCount++;
            if (e.isActive()) {
                activeCredits += e.getCourse().getCreditHours();
            }
            System.out.println("\n" + e.getCourse().getDisplayName() + " (" + e.getCourse().getCreditHours()
                    + " credits) | " + e.getSemester() + " | " + e.getEnrollmentStatus());
            double obtained = 0;
            double maximum = 0;
            int assessed = 0;
            for (Assessment a : assessments) {
                if (a.getEnrollment() == e) {
                    assessed++;
                    obtained += a.getObtainedScore();
                    maximum += a.getMaximumScore();
                    System.out.println("   " + a.getAssessmentType() + ": " + a.getObtainedScore()
                            + "/" + a.getMaximumScore() + "  (" + a.getAssessmentDate() + ")");
                }
            }
            if (assessed == 0) {
                System.out.println("   No assessments recorded.");
            } else {
                System.out.println("   Course score: " + obtained + "/" + maximum
                        + " = " + String.format("%.1f", obtained * 100 / maximum) + "%");
            }
        }
        if (enrollmentCount == 0) {
            System.out.println("\nNo enrollments recorded for this student.");
        }
        System.out.println("\nEnrollments: " + enrollmentCount + " | Credit hours in active courses: " + activeCredits);
    }

    private static void showPaymentHistory() {
        Student s = chooseStudent("SELECT A STUDENT");
        if (s == null) {
            return;
        }
        System.out.println("\n========== PAYMENT HISTORY: " + s.getDisplayName() + " ==========");
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        for (FeePayment p : payments) {
            if (p.getStudent() == s) {
                count++;
                total = total.add(p.getAmount());
                System.out.println("  " + p.getPaymentDate() + " | " + p.getAmount() + " | "
                        + p.getPaymentMethod() + " | receipt " + p.getReceiptNumber());
            }
        }
        if (count == 0) {
            System.out.println("  No payments recorded for this student.");
        } else {
            System.out.println("Payments: " + count + " | Total paid: " + total);
        }
    }

    private static void viewAvailableBooks() {
        System.out.println("\n========== AVAILABLE BOOKS ==========");
        int count = 0;
        for (LibraryBook b : books) {
            if (b.getAvailableCopies() > 0) {
                System.out.println("  " + b.getRegistrationSummary());
                count++;
            }
        }
        if (count == 0) {
            System.out.println("  No book has an available copy right now.");
        }
    }

    // =====================================================================
    //  19. DEMONSTRATE POLYMORPHISM
    // =====================================================================
    private static void demonstratePolymorphism() {
        System.out.println("\n===== 1. SUPERCLASS POLYMORPHISM: List<Person> =====");
        List<Person> people = allPeople();    // Students, Teachers and Administrators in one list
        if (people.isEmpty()) {
            System.out.println("(No people registered yet. Register a student, teacher or administrator first.)");
        }
        for (Person p : people) {
            // the same call runs a different overridden method depending on the real object
            System.out.println(p.getRoleDescription() + ": " + p.getDisplayName() + " | active: " + p.isActive());
            System.out.println("   summary: " + p.getRegistrationSummary());
            if (p instanceof AcademicMember) {
                System.out.println("   academic category: " + ((AcademicMember) p).getAcademicCategory());
            }
        }

        System.out.println("\n===== 2. INTERFACE POLYMORPHISM: List<Identifiable> =====");
        List<Identifiable> things = new ArrayList<>();
        things.addAll(people);
        things.addAll(departments);
        things.addAll(courses);
        things.addAll(classrooms);
        things.addAll(enrollments);
        things.addAll(assessments);
        things.addAll(payments);
        things.addAll(books);
        things.addAll(loans);
        if (things.isEmpty()) {
            System.out.println("(No records registered yet.)");
        }
        for (Identifiable item : things) {
            System.out.println(item.getClass().getSimpleName() + " | id: " + item.getId()
                    + " | " + item.getDisplayName() + " | active: " + item.isActive());
        }

        System.out.println("\n===== 3. WHY Person AND AcademicMember CANNOT BE INSTANTIATED =====");
        System.out.println("Both are abstract. They hold shared fields and rules, but getRoleDescription() and");
        System.out.println("getAcademicCategory() have no body, so 'new Person(...)' and 'new AcademicMember(...)'");
        System.out.println("are compile errors. Only complete classes (Student, Teacher, Administrator) can be created.");
        System.out.println("Constructor chaining: Student -> super(...) -> AcademicMember -> super(...) -> Person.");
    }

    // =====================================================================
    //  MENU
    // =====================================================================
    private static void printMenu() {
        String[] left = {
            " 1. Register Department", " 2. Register Student", " 3. Register Teacher",
            " 4. Register Administrator", " 5. Register Course", " 6. Register Classroom",
            " 7. Enroll Student", " 8. Record Assessment", " 9. Record Fee Payment",
            "10. Register Library Book"
        };
        String[] right = {
            "11. Borrow Library Book", "12. Return Library Book", "13. View All Records",
            "14. Search Record by ID", "15. Update Existing Record", "16. Student Academic Summary",
            "17. Student Payment History", "18. View Available Books", "19. Demonstrate Polymorphism",
            " 0. Exit"
        };
        System.out.println("\n============== AUCA_ID SCHOOL MANAGEMENT ==============");
        for (int i = 0; i < left.length; i++) {
            System.out.println(String.format("%-30s %s", left[i], right[i]));
        }
    }

    public static void main(String[] args) {
        boolean running = true;
        while (running) {
            printMenu();
            String input = ask("Select an option: ");
            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("! Invalid selection. Enter a number from 0 to 19.");
                continue;
            }

            try {
                switch (choice) {
                    case 1: registerFlow("DEPARTMENT"); break;
                    case 2: registerFlow("STUDENT"); break;
                    case 3: registerFlow("TEACHER"); break;
                    case 4: registerFlow("ADMINISTRATOR"); break;
                    case 5: registerFlow("COURSE"); break;
                    case 6: registerFlow("CLASSROOM"); break;
                    case 7: registerFlow("ENROLLMENT"); break;
                    case 8: registerFlow("ASSESSMENT"); break;
                    case 9: registerFlow("FEEPAYMENT"); break;
                    case 10: registerFlow("BOOK"); break;
                    case 11: registerFlow("BOOKLOAN"); break;
                    case 12: returnBook(); break;
                    case 13: viewAllRecords(); break;
                    case 14: searchById(); break;
                    case 15: updateRecord(); break;
                    case 16: showAcademicSummary(); break;
                    case 17: showPaymentHistory(); break;
                    case 18: viewAvailableBooks(); break;
                    case 19: demonstratePolymorphism(); break;
                    case 0:
                        running = false;
                        System.out.println("Goodbye!");
                        break;
                    default:
                        System.out.println("! Option out of range. Enter a number from 0 to 19.");
                }
            } catch (RuntimeException e) {
                System.out.println("! Operation failed: " + e.getMessage() + ". Returning to the menu.");
            }
        }
    }
}