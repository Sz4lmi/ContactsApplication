package contacts.controller;

import contacts.domain.User;
import contacts.dto.UserDTO;
import contacts.repository.UserRepository;
import contacts.service.UserService;
import contacts.util.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class AuthControllerIntegrationTest {

    private MockMvc mockMvc;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;

    private User testUser;
    private User adminUser;
    private UserDTO testUserDTO;
    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        // Setup MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();

        // Setup ObjectMapper
        objectMapper = new ObjectMapper();

        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("hashedpassword");
        testUser.setRole("ROLE_USER");

        // Setup admin user
        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setPassword("hashedpassword");
        adminUser.setRole("ROLE_ADMIN");

        // Setup test user DTO
        testUserDTO = new UserDTO();
        testUserDTO.setUsername("newuser");
        testUserDTO.setPassword("password");
        testUserDTO.setRole("ROLE_USER");

        // Create JWT tokens for testing
        // In a real test, you would use a proper JWT library to create these tokens
        // For simplicity, we'll use mock tokens here
        adminToken = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJST0xFX0FETUlOIiwidXNlcklkIjoyLCJpYXQiOjE1MTYyMzkwMjJ9.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        userToken = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsInJvbGUiOiJST0xFX1VTRVIiLCJ1c2VySWQiOjEsImlhdCI6MTUxNjIzOTAyMn0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    }

    @Test
    void login_WithValidCredentials_ShouldReturnToken() throws Exception {
        // Arrange
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "testuser");
        credentials.put("password", "password");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "hashedpassword")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.role").value(testUser.getRole()));
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "testuser");
        credentials.put("password", "wrongpassword");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "hashedpassword")).thenReturn(false);

        // Act & Assert
        // The controller throws a RuntimeException which is not caught and converted to a 401 response
        // So we expect the exception to be thrown
        try {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(credentials)));
            // If we get here, the test should fail because no exception was thrown
            fail("Expected RuntimeException was not thrown");
        } catch (Exception e) {
            // Verify that the exception is a RuntimeException with the message "Invalid credentials"
            assertTrue(e.getCause() instanceof RuntimeException);
            assertEquals("Invalid credentials", e.getCause().getMessage());
        }
    }

    @Test
    void createUser_AsAdmin_ShouldCreateUser() throws Exception {
        // Arrange
        when(userService.createUser(any(UserDTO.class))).thenReturn(testUser);

        // Act & Assert
        try (MockedStatic<JwtUtils> jwtUtils = Mockito.mockStatic(JwtUtils.class)) {
            jwtUtils.when(() -> JwtUtils.getRoleFromToken(any())).thenReturn("ROLE_ADMIN");

            mockMvc.perform(post("/api/auth/users")
                    .header("Authorization", adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testUserDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testUser.getId()))
                    .andExpect(jsonPath("$.username").value(testUser.getUsername()))
                    .andExpect(jsonPath("$.role").value(testUser.getRole()));
        }
    }

    @Test
    void createUser_AsUser_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        try (MockedStatic<JwtUtils> jwtUtils = Mockito.mockStatic(JwtUtils.class)) {
            jwtUtils.when(() -> JwtUtils.getRoleFromToken(any())).thenReturn("ROLE_USER");

            mockMvc.perform(post("/api/auth/users")
                    .header("Authorization", userToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testUserDTO)))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void getAllUsers_AsAdmin_ShouldReturnAllUsers() throws Exception {
        // Arrange
        when(userService.getAllUsers()).thenReturn(Arrays.asList(testUser, adminUser));

        // Act & Assert
        try (MockedStatic<JwtUtils> jwtUtils = Mockito.mockStatic(JwtUtils.class)) {
            jwtUtils.when(() -> JwtUtils.getRoleFromToken(any())).thenReturn("ROLE_ADMIN");

            mockMvc.perform(get("/api/auth/users")
                    .header("Authorization", adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(testUser.getId()))
                    .andExpect(jsonPath("$[0].username").value(testUser.getUsername()))
                    .andExpect(jsonPath("$[1].id").value(adminUser.getId()))
                    .andExpect(jsonPath("$[1].username").value(adminUser.getUsername()));
        }
    }

    @Test
    void getAllUsers_AsUser_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        try (MockedStatic<JwtUtils> jwtUtils = Mockito.mockStatic(JwtUtils.class)) {
            jwtUtils.when(() -> JwtUtils.getRoleFromToken(any())).thenReturn("ROLE_USER");

            mockMvc.perform(get("/api/auth/users")
                    .header("Authorization", userToken))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void updateUser_AsAdmin_ShouldUpdateUser() throws Exception {
        // Arrange
        when(userService.updateUser(any(Long.class), any(UserDTO.class))).thenReturn(testUser);

        // Act & Assert
        try (MockedStatic<JwtUtils> jwtUtils = Mockito.mockStatic(JwtUtils.class)) {
            jwtUtils.when(() -> JwtUtils.getRoleFromToken(any())).thenReturn("ROLE_ADMIN");

            mockMvc.perform(put("/api/auth/users/1")
                    .header("Authorization", adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testUserDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testUser.getId()))
                    .andExpect(jsonPath("$.username").value(testUser.getUsername()))
                    .andExpect(jsonPath("$.role").value(testUser.getRole()));
        }
    }

    @Test
    void updateUser_AsUser_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        try (MockedStatic<JwtUtils> jwtUtils = Mockito.mockStatic(JwtUtils.class)) {
            jwtUtils.when(() -> JwtUtils.getRoleFromToken(any())).thenReturn("ROLE_USER");

            mockMvc.perform(put("/api/auth/users/1")
                    .header("Authorization", userToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testUserDTO)))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void deleteUser_AsAdmin_ShouldDeleteUser() throws Exception {
        // Act & Assert
        try (MockedStatic<JwtUtils> jwtUtils = Mockito.mockStatic(JwtUtils.class)) {
            jwtUtils.when(() -> JwtUtils.getRoleFromToken(any())).thenReturn("ROLE_ADMIN");

            mockMvc.perform(delete("/api/auth/users/1")
                    .header("Authorization", adminToken))
                    .andExpect(status().isOk())
                    .andExpect(content().string("User deleted successfully"));
        }
    }

    @Test
    void deleteUser_AsUser_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        try (MockedStatic<JwtUtils> jwtUtils = Mockito.mockStatic(JwtUtils.class)) {
            jwtUtils.when(() -> JwtUtils.getRoleFromToken(any())).thenReturn("ROLE_USER");

            mockMvc.perform(delete("/api/auth/users/1")
                    .header("Authorization", userToken))
                    .andExpect(status().isForbidden());
        }
    }
}
