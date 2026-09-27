package com.bankapp.controller;

import com.bankapp.dto.request.LoginRequest;
import com.bankapp.dto.request.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_thenLogin_shouldSucceedEndToEnd() throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setUsername("alice_test");
        register.setEmail("alice_test@example.com");
        register.setPassword("Password123");
        register.setFirstName("Alice");
        register.setLastName("Tester");
        register.setPhoneNumber("9998887777");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("alice_test"));

        LoginRequest login = new LoginRequest();
        login.setUsername("alice_test");
        login.setPassword("Password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.roles[0]").value("ROLE_CUSTOMER"));
    }

    @Test
    void register_shouldFail_whenPasswordTooShort() throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setUsername("bobby");
        register.setEmail("bobby@example.com");
        register.setPassword("123");
        register.setFirstName("Bob");
        register.setLastName("Smith");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void login_shouldFail_withWrongPassword() throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setUsername("carl_test");
        register.setEmail("carl_test@example.com");
        register.setPassword("Password123");
        register.setFirstName("Carl");
        register.setLastName("Jones");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        LoginRequest login = new LoginRequest();
        login.setUsername("carl_test");
        login.setPassword("WrongPassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }
}
