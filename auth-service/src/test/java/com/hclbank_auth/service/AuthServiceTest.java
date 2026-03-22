package com.hclbank_auth.service;

import com.hclbank_auth.config.JwtUtil;
import com.hclbank_auth.dto.*;
import com.hclbank_auth.entity.Customer;
import com.hclbank_auth.exception.*;
import com.hclbank_auth.repository.CustomerRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)   // no Spring context — pure Mockito
class AuthServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    // ── Helper — builds a valid SignupRequest ──────────────────────────
    private SignupRequest validSignupRequest() {
        return new SignupRequest(
                "John Doe",
                "john@example.com",
                "9876543210",
                "Password@123",
                "452100010001",
                "SBIN0001234"
        );
    }

    // ── Helper — builds a Customer entity ─────────────────────────────
    private Customer savedCustomer() {
        Customer c = new Customer();
        c.setId(UUID.randomUUID());
        c.setCustomerId("CUST000001");
        c.setPasswordHash("$2b$10$hashedpassword");
        c.setFullName("John Doe");
        c.setEmail("john@example.com");
        c.setPhone("9876543210");
        c.setActive(true);
        return c;
    }

    // ════════════════════════════════════════════════════════════════════
    // SIGNUP TESTS
    // ════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Signup — success — returns CUST000001 and token")
    void signup_success() {
        // ARRANGE
        SignupRequest req = validSignupRequest();
        when(customerRepository.existsByEmail(req.getEmail()))
                .thenReturn(false);
        when(customerRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(req.getPassword()))
                .thenReturn("$2b$10$hashed");
        when(customerRepository.save(any(Customer.class)))
                .thenAnswer(inv -> {
                    Customer c = inv.getArgument(0);
                    c.setId(UUID.randomUUID());
                    return c;
                });
        when(jwtUtil.generateToken(anyString(), any(UUID.class)))
                .thenReturn("mock-jwt-token");

        // ACT
        SignupResponse response = authService.signup(req);

        // ASSERT
        assertThat(response.getCustomerId()).isEqualTo("CUST000001");
        assertThat(response.getToken()).isEqualTo("mock-jwt-token");
        assertThat(response.getMessage()).contains("CUST000001");

        // Verify repository was called to save
        verify(customerRepository, times(1)).save(any(Customer.class));
        verify(passwordEncoder, times(1)).encode("Password@123");
    }

    @Test
    @DisplayName("Signup — customer ID increments — second user gets CUST000002")
    void signup_customerIdIncrements() {
        when(customerRepository.existsByEmail(anyString()))
                .thenReturn(false);
        when(customerRepository.count()).thenReturn(1L); // 1 existing
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(customerRepository.save(any())).thenAnswer(inv -> {
            Customer c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });
        when(jwtUtil.generateToken(anyString(), any())).thenReturn("token");

        SignupResponse response = authService.signup(validSignupRequest());

        assertThat(response.getCustomerId()).isEqualTo("CUST000002");
    }

    @Test
    @DisplayName("Signup — duplicate email — throws DuplicateEmailException")
    void signup_duplicateEmail_throwsException() {
        when(customerRepository.existsByEmail("john@example.com"))
                .thenReturn(true); // email already exists

        assertThatThrownBy(() ->
                authService.signup(validSignupRequest()))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("already registered");

        // Repository save must NEVER be called if email is duplicate
        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Signup — password is hashed — plain text never saved")
    void signup_passwordIsHashed() {
        when(customerRepository.existsByEmail(anyString()))
                .thenReturn(false);
        when(customerRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode("Password@123"))
                .thenReturn("$2b$10$BCRYPT_HASH");
        when(customerRepository.save(any())).thenAnswer(inv -> {
            Customer c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });
        when(jwtUtil.generateToken(anyString(), any())).thenReturn("token");

        authService.signup(validSignupRequest());

        // Capture what was saved and verify password hash, not plain text
        ArgumentCaptor<Customer> captor =
                ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(captor.capture());

        Customer saved = captor.getValue();
        assertThat(saved.getPasswordHash())
                .isEqualTo("$2b$10$BCRYPT_HASH")
                .isNotEqualTo("Password@123"); // must NOT be plain text
    }

    // ════════════════════════════════════════════════════════════════════
    // LOGIN TESTS
    // ════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Login — success — returns token and customer info")
    void login_success() {
        Customer customer = savedCustomer();
        LoginRequest req = new LoginRequest("CUST000001", "Password@123");

        when(customerRepository.findByCustomerId("CUST000001"))
                .thenReturn(Optional.of(customer));
        when(passwordEncoder.matches("Password@123", customer.getPasswordHash()))
                .thenReturn(true);
        when(jwtUtil.generateToken(anyString(), any()))
                .thenReturn("valid-jwt-token");

        LoginResponse response = authService.login(req);

        assertThat(response.getToken()).isEqualTo("valid-jwt-token");
        assertThat(response.getCustomerId()).isEqualTo("CUST000001");
        assertThat(response.getFullName()).isEqualTo("John Doe");
        assertThat(response.getExpiresIn()).isEqualTo(1800);
    }

    @Test
    @DisplayName("Login — customer ID not found — throws InvalidCredentialsException")
    void login_customerNotFound_throwsException() {
        when(customerRepository.findByCustomerId("CUST999999"))
                .thenReturn(Optional.empty()); // not found

        assertThatThrownBy(() ->
                authService.login(new LoginRequest("CUST999999", "any")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid Customer ID or password");
    }

    @Test
    @DisplayName("Login — wrong password — throws InvalidCredentialsException")
    void login_wrongPassword_throwsException() {
        Customer customer = savedCustomer();
        when(customerRepository.findByCustomerId("CUST000001"))
                .thenReturn(Optional.of(customer));
        when(passwordEncoder.matches("WrongPassword", customer.getPasswordHash()))
                .thenReturn(false); // bcrypt compare fails

        assertThatThrownBy(() ->
                authService.login(new LoginRequest("CUST000001", "WrongPassword")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid Customer ID or password");

        // JWT must never be issued for wrong password
        verify(jwtUtil, never()).generateToken(anyString(), any());
    }

    @Test
    @DisplayName("Login — inactive account — throws AccountInactiveException")
    void login_inactiveAccount_throwsException() {
        Customer customer = savedCustomer();
        customer.setActive(false); // account blocked

        when(customerRepository.findByCustomerId("CUST000001"))
                .thenReturn(Optional.of(customer));

        assertThatThrownBy(() ->
                authService.login(new LoginRequest("CUST000001", "Password@123")))
                .isInstanceOf(AccountInactiveException.class)
                .hasMessageContaining("inactive");

        // Must not even check password if account is inactive
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Login — last_login_at is updated on success")
    void login_updatesLastLoginAt() {
        Customer customer = savedCustomer();
        when(customerRepository.findByCustomerId("CUST000001"))
                .thenReturn(Optional.of(customer));
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(true);
        when(jwtUtil.generateToken(anyString(), any())).thenReturn("token");

        authService.login(new LoginRequest("CUST000001", "Password@123"));

        // Verify updateLastLogin was called with the right customer ID
        verify(customerRepository, times(1))
                .updateLastLogin(eq("CUST000001"), any());
    }
}