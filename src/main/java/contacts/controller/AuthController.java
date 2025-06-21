package contacts.controller;

import contacts.config.SecurityConstants;
import contacts.domain.User;
import contacts.dto.UserListDTO;
import contacts.dto.UserRequestDTO;
import contacts.repository.UserRepository;
import contacts.service.UserService;
import contacts.util.JwtUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserService userService;

    private final SecretKey SECRET_KEY = SecurityConstants.SECRET_KEY;

    @Autowired
    public AuthController(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, UserService userService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    /**
     * Authenticate a user and generate a JWT token.
     *
     * @param credentials Map containing username and password
     * @param response HTTP response
     * @return Map containing token, userId, and role
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials, HttpServletResponse response) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new RuntimeException("Invalid credentials");
            }

            String jwt = Jwts.builder()
                    .setSubject(username)
                    .claim("role", user.getRole())
                    .claim("userId", user.getId())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day
                    .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                    .compact();
            Map<String, Object> result = new HashMap<>();
            result.put("token", jwt);
            result.put("userId", user.getId());
            result.put("role", user.getRole());
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            // Let the global exception handler handle this
            throw e;
        }
    }

    /**
     * Create a new user (admin only)
     * @param userDTO User data
     * @param request HTTP request
     * @return Created user
     */
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody UserRequestDTO userDTO, HttpServletRequest request) {
        // Check if the current user is an admin
        String role = getRoleFromToken(request);
        if (role == null || !role.equals("ROLE_ADMIN")) {
            return ResponseEntity.status(403).body("Only admins can create users");
        }

        try {
            User createdUser = userService.createUser(userDTO);
            return ResponseEntity.ok(userService.convertToUserListDTO(createdUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Extract role from JWT token
     * @param request HTTP request
     * @return Role or null if not found
     */
    private String getRoleFromToken(HttpServletRequest request) {
        return JwtUtils.getRoleFromToken(request);
    }

    /**
     * Get all users (admin only)
     * @param request HTTP request
     * @return List of all users
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(HttpServletRequest request) {
        // Check if the current user is an admin
        String role = getRoleFromToken(request);
        if (role == null || !role.equals("ROLE_ADMIN")) {
            return ResponseEntity.status(403).body("Only admins can view all users");
        }

        List<UserListDTO> users = userService.getAllUsersAsList();
        return ResponseEntity.ok(users);
    }

    /**
     * Update a user (admin only)
     * @param id User ID
     * @param dto User data
     * @param request HTTP request
     * @return Updated user
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserRequestDTO dto, HttpServletRequest request) {
        // Check if the current user is an admin
        String role = getRoleFromToken(request);
        if (role == null || !role.equals("ROLE_ADMIN")) {
            return ResponseEntity.status(403).body("Only admins can update users");
        }

        try {
            // Get the admin's username from the token
            String adminUsername = JwtUtils.getUsernameFromToken(request);
            User updatedUser = userService.updateUser(id, dto, adminUsername);
            return ResponseEntity.ok(userService.convertToUserListDTO(updatedUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Delete a user (admin only)
     * @param id User ID
     * @param request HTTP request
     * @return Success message
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        // Check if the current user is an admin
        String role = getRoleFromToken(request);
        if (role == null || !role.equals("ROLE_ADMIN")) {
            return ResponseEntity.status(403).body("Only admins can delete users");
        }

        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("User deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
