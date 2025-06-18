package contacts.service;

import contacts.domain.User;
import contacts.dto.UserDTO;
import contacts.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Get all users from the database
     * @return List of all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get a user by ID
     * @param id User ID
     * @return User if found, null otherwise
     */
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    /**
     * Get a user by username
     * @param username Username
     * @return Optional containing the user if found
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Create a new user
     * @param userDTO User data transfer object
     * @return Created user
     */
    public User createUser(UserDTO userDTO) {
        // Check if username already exists
        if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        
        // Set role, default to ROLE_USER if not specified
        String role = userDTO.getRole();
        if (role == null || role.isEmpty()) {
            role = "ROLE_USER";
        }
        user.setRole(role);

        return userRepository.save(user);
    }

    /**
     * Update an existing user
     * @param id User ID
     * @param userDTO User data transfer object
     * @return Updated user
     */
    public User updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update username if provided and not already taken by another user
        if (userDTO.getUsername() != null && !userDTO.getUsername().isEmpty()) {
            Optional<User> existingUser = userRepository.findByUsername(userDTO.getUsername());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
                throw new RuntimeException("Username already exists");
            }
            user.setUsername(userDTO.getUsername());
        }

        // Update password if provided
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        // Update role if provided
        if (userDTO.getRole() != null && !userDTO.getRole().isEmpty()) {
            user.setRole(userDTO.getRole());
        }

        return userRepository.save(user);
    }

    /**
     * Delete a user
     * @param id User ID
     */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}