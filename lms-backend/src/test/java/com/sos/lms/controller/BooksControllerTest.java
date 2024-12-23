package com.sos.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.lms.config.TestConfig;
import com.sos.lms.dao.BooksRepository;
import com.sos.lms.entity.Books;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfig.class)
class BooksControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BooksRepository booksRepository;

    private Books testBook;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        // Explicitly build MockMvc with security configuration
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        testBook = new Books();
        testBook.setBookId(1);
        testBook.setBookName("Test Book");
        testBook.setBookAuthor("Test Author");
        testBook.setBookGenre("Test Genre");
        testBook.setNoOfCopies(5);
    }

    @Test
    @WithMockUser  // Add this to bypass security for this test
    void getAllBooks_ShouldReturnListOfBooks() throws Exception {
        when(booksRepository.findAll()).thenReturn(Arrays.asList(testBook));

        mockMvc.perform(get("/admin/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookName").value("Test Book"))
                .andExpect(jsonPath("$[0].bookAuthor").value("Test Author"));
    }

    @Test
    @WithMockUser
    void getBookById_WhenBookExists_ShouldReturnBook() throws Exception {
        when(booksRepository.findById(1)).thenReturn(Optional.of(testBook));

        mockMvc.perform(get("/admin/books/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookName").value("Test Book"));
    }

    @Test
    @WithMockUser(roles = "Admin")
    void createBook_ShouldReturnSavedBook() throws Exception {
        when(booksRepository.save(any(Books.class))).thenReturn(testBook);

        mockMvc.perform(post("/admin/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBook)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookName").value("Test Book"));
    }

    @Test
    @WithMockUser(roles = "Admin")
    void updateBook_WhenBookExists_ShouldReturnUpdatedBook() throws Exception {
        Books updatedBook = new Books();
        updatedBook.setBookName("Updated Book");

        when(booksRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(booksRepository.save(any(Books.class))).thenReturn(updatedBook);

        mockMvc.perform(put("/admin/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedBook)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookName").value("Updated Book"));
    }

    @Test
    @WithMockUser(roles = "Admin")
    void deleteBook_WhenBookExists_ShouldReturnSuccess() throws Exception {
        when(booksRepository.findById(1)).thenReturn(Optional.of(testBook));

        mockMvc.perform(delete("/admin/books/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted").value(true));
    }

    @Test
    @WithMockUser
    void getBookById_WhenBookDoesNotExist_ShouldThrowNotFoundException() throws Exception {
        when(booksRepository.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/books/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}