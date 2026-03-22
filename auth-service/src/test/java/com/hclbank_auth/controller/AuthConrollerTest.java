package com.hclbank_auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hclbank_auth.config.JwtUtil;
import com.hclbank_auth.config.SecurityConfig;
import com.hclbank_auth.dto.*;
import com.hclbank_auth.exception.*;
import com.hclbank_auth.service.AuthService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtUtil.class})
class AuthControllerTest {                                    // ← class opens {

    @Autowired private MockMvc      mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private AuthService  authService;

    // ── TEST 1 ────────────────────────────────────────────────────────
    @Test
    @DisplayName("POST /auth/signup — 201 Created")
    void signup_returns201() throws Exception {
        SignupRequest req = new SignupRequest(
                "John Doe", "john@example.com", "9876543210",
                "Password@123", "452100010001", "SBIN0001234");

        when(authService.signup(any()))
                .thenReturn(new SignupResponse(
                        "CUST000001", "mock-token", "Account created!"));

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value("CUST000001"))
                .andExpect(jsonPath("$.token").value("mock-token"));
    }                                                              // ← method closes }

    // ── TEST 2 ────────────────────────────────────────────────────────
    @Test
    @DisplayName("POST /auth/signup — 400 when email is invalid")
    void signup_invalidEmail_returns400() throws Exception {
        SignupRequest req = new SignupRequest(
                "John Doe", "not-an-email", "9876543210",
                "Password@123", "452100010001", "SBIN0001234");

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        verify(authService, never()).signup(any());
    }                                                              // ← method closes }

    // ── TEST 3 ────────────────────────────────────────────────────────
    @Test
    @DisplayName("POST /auth/signup — 400 when phone is invalid")
    void signup_invalidPhone_returns400() throws Exception {
        SignupRequest req = new SignupRequest(
                "John Doe", "john@example.com", "12345",
                "Password@123", "452100010001", "SBIN0001234");

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        verify(authService, never()).signup(any());
    }                                                              // ← method closes }

    // ── TEST 4 ────────────────────────────────────────────────────────
    @Test
    @DisplayName("POST /auth/signup — 400 when password is weak")
    void signup_weakPassword_returns400() throws Exception {
        SignupRequest req = new SignupRequest(
                "John Doe", "john@example.com", "9876543210",
                "weak", "452100010001", "SBIN0001234");

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).signup(any());
    }                                                              // ← method closes }

    // ── TEST 5 ────────────────────────────────────────────────────────
    @Test
    @DisplayName("POST /auth/signup — 400 when IFSC is invalid")
    void signup_invalidIfsc_returns400() throws Exception {
        SignupRequest req = new SignupRequest(
                "John Doe", "john@example.com", "9876543210",
                "Password@123", "452100010001", "INVALID");

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).signup(any());
    }                                                              // ← method closes }

    // ── TEST 6 ────────────────────────────────────────────────────────
    @Test
    @DisplayName("POST /auth/signup — 409 duplicate email")
    void signup_duplicateEmail_returns409() throws Exception {
        when(authService.signup(any()))
                .thenThrow(new DuplicateEmailException(
                        "Email already registered"));

        SignupRequest req = new SignupRequest(
                "John Doe", "john@example.com", "9876543210",
                "Password@123", "452100010001", "SBIN0001234");

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("DUPLICATE_EMAIL"));
    }                                                              // ← method closes }

    // ── TEST 7 ────────────────────────────────────────────────────────
    @Test
    @DisplayName("POST /auth/login — 200 OK with token")
    void login_returns200() throws Exception {
        when(authService.login(any()))
                .thenReturn(new LoginResponse(
                        "mock-token", "CUST000001", "John Doe", 1800));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"CUST000001\",\"password\":\"Password@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-token"))
                .andExpect(jsonPath("$.customerId").value("CUST000001"))
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.expiresIn").value(1800));
    }                                                              // ← method closes }

    // ── TEST 8 ────────────────────────────────────────────────────────
    @Test
    @DisplayName("POST /auth/login — 401 wrong credentials")
    void login_wrongCredentials_returns401() throws Exception {
        when(authService.login(any()))
                .thenThrow(new InvalidCredentialsException(
                        "Invalid Customer ID or password"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"CUST000001\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));
    }                                                              // ← method closes }

    // ── TEST 9 ────────────────────────────────────────────────────────
    @Test
    @DisplayName("POST /auth/login — 403 account inactive")
    void login_inactiveAccount_returns403() throws Exception {
        when(authService.login(any()))
                .thenThrow(new AccountInactiveException(
                        "Account inactive."));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"CUST000001\",\"password\":\"Password@123\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCOUNT_INACTIVE"));
    }                                                              // ← method closes }

    // ── TEST 10 ───────────────────────────────────────────────────────
    @Test
    @DisplayName("POST /auth/login — 400 when customerId is blank")
    void login_blankCustomerId_returns400() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"\",\"password\":\"Password@123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        verify(authService, never()).login(any());
    }                                                              // ← method closes }

}