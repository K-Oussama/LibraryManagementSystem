package com.sos.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.lms.config.TestConfig;
import com.sos.lms.dao.UsersRepository;
import com.sos.lms.entity.Role;
import com.sos.lms.entity.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
@Import(TestConfig.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsersRepository usersRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    private Users testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setRoleId(1);
        testRole.setRoleName("Admin");

        testUser = new Users();
        testUser.setUserId(1);
        testUser.setName("Test Admin");
        testUser.setUsername("admin");
        testUser.setPassword("password");
        testUser.setRole(new HashSet<>(Collections.singletonList(testRole)));
    }

    @Test
    @WithMockUser(roles = "Admin")
    void addUserByAdmin_ShouldCreateUser() throws Exception {
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(usersRepository.save(any(Users.class))).thenReturn(testUser);

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Admin"));
    }

    @Test
    @WithMockUser(roles = "Admin")
    void getAllUsers_ShouldReturnUsersList() throws Exception {
        when(usersRepository.findAll()).thenReturn(Arrays.asList(testUser));

        mockMvc.perform(get("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Admin"));
    }

    @Test
    @WithMockUser(roles = "Admin")
    void getUserById_WhenUserExists_ShouldReturnUser() throws Exception {
        when(usersRepository.findById(1)).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/admin/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Admin"));
    }

    @Test
    @WithMockUser(roles = "Admin")
    void getUserById_WhenUserDoesNotExist_ShouldReturnNotFound() throws Exception {
        when(usersRepository.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/users/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "Admin")
    void updateUser_WhenUserExists_ShouldUpdateUser() throws Exception {
        Users updatedUser = new Users();
        updatedUser.setName("Updated Name");

        when(usersRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(usersRepository.save(any(Users.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/admin/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    @WithMockUser(roles = "Admin")
    void updateUser_WhenUserDoesNotExist_ShouldReturnNotFound() throws Exception {
        when(usersRepository.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(put("/admin/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isNotFound());
    }
}