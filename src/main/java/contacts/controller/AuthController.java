package contacts.controller;

import contacts.config.SecurityConstants;
import contacts.domain.User;
import contacts.dto.UserDTO;
import contacts.repository.UserRepository;
import contacts.service.UserService;
import io.jsonwebtoken.Claims;
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

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> credentials, HttpServletResponse response) {
        String username = credentials.get("username");
        String password = credentials.get("password");


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
        return result;
    }

    /**
     * Create a new user (admin only)
     * @param userDTO User data
     * @param request HTTP request
     * @return Created user
     */
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody UserDTO userDTO, HttpServletRequest request) {
        // Check if the current user is an admin
        String role = getRoleFromToken(request);
        if (role == null || !role.equals("ROLE_ADMIN")) {
            return ResponseEntity.status(403).body("Only admins can create users");
        }

        try {
            User createdUser = userService.createUser(userDTO);
            return ResponseEntity.ok(createdUser);
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
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            try {
                Claims claims = Jwts.parser()
                        .setSigningKey(SecurityConstants.SECRET_KEY)
                        .parseClaimsJws(token)
                        .getBody();

                // Get role from claims
                return claims.get("role", String.class);
            } catch (Exception e) {
                // Token validation failed
                return null;
            }
        }
        return null;
    }
}
