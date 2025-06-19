package contacts.service;

import contacts.domain.User;
import contacts.dto.UserDTO;
import contacts.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("hashedpassword");
        testUser.setRole("ROLE_USER");

        // Setup test user DTO
        testUserDTO = new UserDTO();
        testUserDTO.setUsername("testuser");
        testUserDTO.setPassword("password");
        testUserDTO.setRole("ROLE_USER");
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testUser.getUsername(), result.get(0).getUsername());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldReturnNull() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        User result = userService.getUserById(1L);

        // Assert
        assertNull(result);
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserByUsername_WhenUserExists_ShouldReturnUser() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.getUserByUsername("testuser");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser.getUsername(), result.get().getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void getUserByUsername_WhenUserDoesNotExist_ShouldReturnEmptyOptional() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.getUserByUsername("nonexistent");

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByUsername("nonexistent");
    }

    @Test
    void createUser_WithValidData_ShouldCreateUser() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.createUser(testUserDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
        verify(userRepository, times(1)).findByUsername(testUserDTO.getUsername());
        verify(passwordEncoder, times(1)).encode(testUserDTO.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_WithExistingUsername_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.createUser(testUserDTO);
        });
        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository, times(1)).findByUsername(testUserDTO.getUsername());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_WithValidData_AsUser_ShouldUpdateUser() {
        // Arrange
        UserDTO updateDTO = new UserDTO();
        updateDTO.setUsername("updateduser");
        updateDTO.setPassword("newpassword");
        updateDTO.setOldPassword("password");
        updateDTO.setRole("ROLE_ADMIN");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(eq("password"), anyString())).thenReturn(true);
        when(userRepository.findByUsername("updateduser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("newpassword")).thenReturn("newhashed");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateUser(1L, updateDTO, null); // null adminUsername means user is updating themselves

        // Assert
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches(eq("password"), anyString());
        verify(userRepository).findByUsername("updateduser");
        verify(passwordEncoder).encode("newpassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_WithValidData_AsAdmin_ShouldUpdateUser() {
        // Arrange
        UserDTO updateDTO = new UserDTO();
        updateDTO.setUsername("updateduser");
        updateDTO.setPassword("newpassword");
        updateDTO.setOldPassword("adminpassword");
        updateDTO.setRole("ROLE_ADMIN");

        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setPassword("hashedadminpassword");
        adminUser.setRole("ROLE_ADMIN");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches(eq("adminpassword"), anyString())).thenReturn(true);
        when(userRepository.findByUsername("updateduser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("newpassword")).thenReturn("newhashed");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateUser(1L, updateDTO, "admin"); // admin is updating the user

        // Assert
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(userRepository).findByUsername("admin");
        verify(passwordEncoder).matches(eq("adminpassword"), anyString());
        verify(userRepository).findByUsername("updateduser");
        verify(passwordEncoder).encode("newpassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_WithIncorrectOldPassword_ShouldThrowException() {
        // Arrange
        UserDTO updateDTO = new UserDTO();
        updateDTO.setUsername("updateduser");
        updateDTO.setPassword("newpassword");
        updateDTO.setOldPassword("wrongpassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(updateDTO.getOldPassword(), testUser.getPassword())).thenReturn(false);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.updateUser(1L, updateDTO, null);
        });
        assertEquals("Old password is incorrect", exception.getMessage());
        verify(userRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(1)).matches(updateDTO.getOldPassword(), testUser.getPassword());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_WithExistingUsername_ShouldThrowException() {
        // Arrange
        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setUsername("existinguser");

        UserDTO updateDTO = new UserDTO();
        updateDTO.setUsername("existinguser");
        updateDTO.setPassword("newpassword");
        updateDTO.setOldPassword("password");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(updateDTO.getOldPassword(), testUser.getPassword())).thenReturn(true);
        when(userRepository.findByUsername(updateDTO.getUsername())).thenReturn(Optional.of(existingUser));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.updateUser(1L, updateDTO, null);
        });
        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(1)).matches(updateDTO.getOldPassword(), testUser.getPassword());
        verify(userRepository, times(1)).findByUsername(updateDTO.getUsername());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_ShouldDeleteUser() {
        // Arrange
        doNothing().when(userRepository).deleteById(1L);

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository, times(1)).deleteById(1L);
    }
}
