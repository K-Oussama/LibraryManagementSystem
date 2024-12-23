package com.sos.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.lms.config.TestConfig;
import com.sos.lms.dao.BooksRepository;
import com.sos.lms.dao.BorrowRepository;
import com.sos.lms.dao.UsersRepository;
import com.sos.lms.entity.Books;
import com.sos.lms.entity.Borrow;
import com.sos.lms.entity.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
@Import(TestConfig.class)
class BorrowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BorrowRepository borrowRepository;

    @MockBean
    private UsersRepository usersRepository;

    @MockBean
    private BooksRepository booksRepository;

    private Books testBook;
    private Users testUser;
    private Borrow testBorrow;
    private SimpleDateFormat dateFormat;

    @BeforeEach
    void setUp() {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

        testBook = new Books();
        testBook.setBookId(1);
        testBook.setBookName("Test Book");
        testBook.setBookAuthor("Test Author");
        testBook.setNoOfCopies(2);

        testUser = new Users();
        testUser.setUserId(1);
        testUser.setName("Test User");

        testBorrow = new Borrow();
        testBorrow.setBorrowId(1);
        testBorrow.setBookId(1);
        testBorrow.setUserId(1);
        // We don't set dates here
    }

    @Test
    void borrowBook_WhenBookAvailable_ShouldReturnSuccess() throws Exception {
        when(booksRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(usersRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(borrowRepository.save(any(Borrow.class))).thenReturn(testBorrow);

        String requestJson = "{\"borrowId\":1,\"bookId\":1,\"userId\":1}";

        mockMvc.perform(post("/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("has borrowed one copy of")));

        verify(booksRepository).save(any(Books.class));
        verify(borrowRepository).save(any(Borrow.class));
    }

    @Test
    void borrowBook_WhenBookNotAvailable_ShouldReturnError() throws Exception {
        testBook.setNoOfCopies(0);
        when(booksRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(usersRepository.findById(1)).thenReturn(Optional.of(testUser));

        String requestJson = "{\"borrowId\":1,\"bookId\":1,\"userId\":1}";

        mockMvc.perform(post("/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("out of stock")));

        verify(borrowRepository, never()).save(any(Borrow.class));
    }

    @Test
    void returnBook_ShouldUpdateBookAndBorrow() throws Exception {
        when(borrowRepository.findById(1)).thenReturn(Optional.of(testBorrow));
        when(booksRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(borrowRepository.save(any(Borrow.class))).thenReturn(testBorrow);

        String requestJson = "{\"borrowId\":1,\"bookId\":1,\"userId\":1}";

        mockMvc.perform(put("/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        verify(booksRepository).save(any(Books.class));
        verify(borrowRepository).save(any(Borrow.class));
    }

    @Test
    void getAllBorrow_ShouldReturnList() throws Exception {
        when(borrowRepository.findAll()).thenReturn(Arrays.asList(testBorrow));

        mockMvc.perform(get("/borrow")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].borrowId").value(1));
    }

    @Test
    void booksBorrowedByUser_ShouldReturnUserBorrows() throws Exception {
        when(borrowRepository.findByUserId(1)).thenReturn(Arrays.asList(testBorrow));

        mockMvc.perform(get("/borrow/user/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].borrowId").value(1));
    }

    @Test
    void bookBorrowHistory_ShouldReturnBookBorrows() throws Exception {
        when(borrowRepository.findByBookId(1)).thenReturn(Arrays.asList(testBorrow));

        mockMvc.perform(get("/borrow/book/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].borrowId").value(1));
    }
}