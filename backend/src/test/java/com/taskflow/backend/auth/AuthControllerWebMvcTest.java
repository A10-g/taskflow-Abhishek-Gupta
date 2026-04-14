package com.taskflow.backend.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.backend.auth.dto.AuthResponse;
import com.taskflow.backend.auth.dto.LoginRequest;
import com.taskflow.backend.auth.security.CurrentUserDetailsService;
import com.taskflow.backend.auth.security.JwtAuthenticationFilter;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private CurrentUserDetailsService currentUserDetailsService;

    @Test
    void loginReturnsJwtPayloadForValidCredentials() throws Exception {
        AuthResponse response = new AuthResponse(
                "mock-jwt-token",
                new AuthResponse.AuthUser(
                        UUID.fromString("11111111-1111-1111-1111-111111111111"),
                        "Test User",
                        "test@example.com"
                )
        );
        given(authService.login(any(LoginRequest.class))).willReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("test@example.com", "password123")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"));
    }
}
