package com.hclbank_auth;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.hclbank_auth.dto.*;
import com.hclbank_auth.repository.CustomerRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest                  // loads full Spring context
@AutoConfigureMockMvc            // sets up MockMvc automatically
@ActiveProfiles("test")          // uses application.yml in test/resources
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)  // tests run in order
class AuthServiceIntegrationTest {

    @Autowired private MockMvc            mockMvc;
    @Autowired private ObjectMapper       objectMapper;
    @Autowired private CustomerRepository  customerRepository;

    // Shared state across ordered tests
    private static String generatedCustomerId;
    private static String authToken;

    // ── TEST 1: Full signup flow ─────────────────────────────────────────
    @Test
    @Order(1)
    @DisplayName("Integration — signup creates customer in DB and returns CUST000001")
    void signup_fullFlow() throws Exception {
        SignupRequest req = new SignupRequest(
                "John Doe", "john@test.com", "9876543210",
                "Password@123", "452100010001", "SBIN0001234");

        String responseJson = mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value("CUST000001"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Save for use in subsequent tests
        SignupResponse response =
                objectMapper.readValue(responseJson, SignupResponse.class);
        generatedCustomerId = response.getCustomerId();
        authToken = response.getToken();

        // Verify customer actually saved in H2 DB
        assertThat(customerRepository.count()).isEqualTo(1);
        assertThat(customerRepository
                .findByCustomerId("CUST000001")).isPresent();
    }

    // ── TEST 2: Duplicate signup rejected ───────────────────────────────
    @Test
    @Order(2)
    @DisplayName("Integration — duplicate email returns 409")
    void signup_duplicateEmail_returns409() throws Exception {
        SignupRequest req = new SignupRequest(
                "Jane Doe",
                "john@test.com",   // same email as test 1
                "9876543211",
                "Password@123",
                "452100010002", "SBIN0001234");

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())          // 409
                .andExpect(jsonPath("$.error")
                        .value("DUPLICATE_EMAIL"));

        // Still only 1 customer in DB
        assertThat(customerRepository.count()).isEqualTo(1);
    }

    // ── TEST 3: Login with generated customerId ──────────────────────────
    @Test
    @Order(3)
    @DisplayName("Integration — login with CUST000001 returns JWT token")
    void login_withGeneratedCustomerId() throws Exception {
        LoginRequest req = new LoginRequest(
                generatedCustomerId, "Password@123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.customerId").value(generatedCustomerId))
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.expiresIn").value(1800));
    }

    // ── TEST 4: Wrong password returns 401 ──────────────────────────────
    @Test
    @Order(4)
    @DisplayName("Integration — wrong password returns 401")
    void login_wrongPassword_returns401() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest(generatedCustomerId, "WrongPassword"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error")
                        .value("INVALID_CREDENTIALS"));
    }

    // ── TEST 5: Unknown customerId returns 401 ───────────────────────────
    @Test
    @Order(5)
    @DisplayName("Integration — unknown customer ID returns 401")
    void login_unknownCustomerId_returns401() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("CUST999999", "Password@123"))))
                .andExpect(status().isUnauthorized());
    }

    // ── TEST 6: Second signup gets CUST000002 ────────────────────────────
    @Test
    @Order(6)
    @DisplayName("Integration — second user gets CUST000002")
    void signup_secondUser_getsCUST000002() throws Exception {
        SignupRequest req = new SignupRequest(
                "Jane Doe", "jane@test.com", "9876543211",
                "Password@456", "452100010002", "HDFC0001234");

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value("CUST000002"));

        assertThat(customerRepository.count()).isEqualTo(2);
    }
}