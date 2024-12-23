package com.sos.lms.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BooksTest {

    private Books book;

    @BeforeEach
    void setUp() {
        book = new Books();
        book.setBookId(1);
        book.setBookName("Test Book");
        book.setBookAuthor("Test Author");
        book.setBookGenre("Test Genre");
        book.setNoOfCopies(5);
    }

    @Test
    void testBorrowBook() {
        int initialCopies = book.getNoOfCopies();
        book.borrowBook();
        assertEquals(initialCopies - 1, book.getNoOfCopies());
    }

    @Test
    void testReturnBook() {
        int initialCopies = book.getNoOfCopies();
        book.returnBook();
        assertEquals(initialCopies + 1, book.getNoOfCopies());
    }

    @Test
    void testBookProperties() {
        assertEquals(1, book.getBookId());
        assertEquals("Test Book", book.getBookName());
        assertEquals("Test Author", book.getBookAuthor());
        assertEquals("Test Genre", book.getBookGenre());
        assertEquals(5, book.getNoOfCopies());
    }
}