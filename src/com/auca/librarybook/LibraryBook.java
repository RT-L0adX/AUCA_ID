package com.auca.librarybook;

import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;

public class LibraryBook implements Identifiable, Registrable {
    private String bookId;
    private String isbn;
    private String title;
    private String author;
    private int totalCopies;
    private int availableCopies;

    public LibraryBook(String bookId, String isbn, String title, String author,
                       int totalCopies, int availableCopies) {
        this.bookId = Registrable.clean(bookId);
        this.isbn = Registrable.clean(isbn);
        this.title = Registrable.clean(title);
        this.author = Registrable.clean(author);
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = Registrable.clean(bookId); }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = Registrable.clean(isbn); }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = Registrable.clean(title); }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = Registrable.clean(author); }

    public int getTotalCopies() { return totalCopies; }
    public void setTotalCopies(int totalCopies) { this.totalCopies = totalCopies; }

    public int getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(int availableCopies) { this.availableCopies = availableCopies; }

    @Override
    public String getId() {
        return bookId;
    }

    @Override
    public String getDisplayName() {
        return title + " by " + author;
    }

    @Override
    public boolean isActive() {
        return totalCopies > 0;
    }

    @Override
    public void validate() throws ValidationException {
        Registrable.requireLength("Book ID", bookId, Identifiable.MIN_ID_LENGTH, Identifiable.MAX_ID_LENGTH);
        if (!isValidIsbn(isbn)) {
            throw new ValidationException("ISBN must contain 10 or 13 digits (hyphens allowed).");
        }
        Registrable.requireLength("Title", title, 2, 120);
        Registrable.requireLength("Author", author, 2, 120);
        Registrable.requireMin("Total copies", totalCopies, 0);
        Registrable.requireMin("Available copies", availableCopies, 0);
        if (availableCopies > totalCopies) {
            throw new ValidationException("Available copies cannot exceed total copies.");
        }
    }

    private boolean isValidIsbn(String value) {
        if (value == null) {
            return false;
        }
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

    @Override
    public void printDetails() {
        System.out.println("Book ID       : " + bookId);
        System.out.println("ISBN          : " + isbn);
        System.out.println("Title         : " + title);
        System.out.println("Author        : " + author);
        System.out.println("Total copies  : " + totalCopies);
        System.out.println("Available     : " + availableCopies);
    }

    @Override
    public String getRegistrationSummary() {
        return "[" + bookId + "] " + getDisplayName() + " (" + availableCopies + "/" + totalCopies + " available)";
    }

    @Override
    public String toString() {
        return "LibraryBook [bookId=" + bookId + ", isbn=" + isbn + ", title=" + title + ", author=" + author
                + ", totalCopies=" + totalCopies + ", availableCopies=" + availableCopies + "]";
    }
}