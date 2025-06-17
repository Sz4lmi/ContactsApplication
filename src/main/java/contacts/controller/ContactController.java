package contacts.controller;

import contacts.domain.Contact;
import contacts.dto.ContactRequestDTO;
import contacts.service.ContactService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

import contacts.config.SecurityConstants;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public List<Contact> getAllContacts(HttpServletRequest request) {
        // Extract user ID from JWT token
        Long userId = getUserIdFromToken(request);

        // If we have a userId, filter contacts by user
        if (userId != null) {
            return contactService.getContactsByUserId(userId);
        }

        // Otherwise, return all contacts (this should be restricted in a real app)
        return contactService.getAllContacts();
    }

    @PostMapping
    public Contact createContact(@RequestBody ContactRequestDTO dto, HttpServletRequest request) {
        // Extract user ID from JWT token
        Long userId = getUserIdFromToken(request);
        return contactService.saveContact(dto, userId);
    }

    private Long getUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            try {
                Claims claims = Jwts.parser()
                        .setSigningKey(SecurityConstants.SECRET_KEY)
                        .parseClaimsJws(token)
                        .getBody();

                // Get userId from claims
                Integer userId = claims.get("userId", Integer.class);
                return userId != null ? userId.longValue() : null;
            } catch (Exception e) {
                // Token validation failed
                return null;
            }
        }
        return null;
    }
}
