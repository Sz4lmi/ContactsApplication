package contacts.controller;

import contacts.domain.Contact;
import contacts.dto.ContactListDTO;
import contacts.dto.ContactRequestDTO;
import contacts.service.ContactService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/{id}")
    public ResponseEntity<Contact> getContactById(@PathVariable Long id, HttpServletRequest request) {
        // Extract user ID from JWT token
        Long userId = getUserIdFromToken(request);
        String role = getRoleFromToken(request);

        // Get the contact
        Contact contact = contactService.getContactById(id);

        // Check if user has access to this contact
        if (contact != null && (role != null && role.equals("ROLE_ADMIN") || 
                               (userId != null && contact.getUser() != null && 
                                contact.getUser().getId().equals(userId)))) {
            return ResponseEntity.ok(contact);
        }

        return ResponseEntity.notFound().build();
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

    @GetMapping("/list")
    public List<ContactListDTO> getContactList(HttpServletRequest request) {
        // Extract user ID and role from JWT token
        Long userId = getUserIdFromToken(request);
        String role = getRoleFromToken(request);

        // If user is admin, return all contacts
        if (role != null && role.equals("ROLE_ADMIN")) {
            return contactService.getAllContactsAsList();
        }

        // If we have a userId, get contacts for that user
        if (userId != null) {
            return contactService.getContactListByUserId(userId);
        }

        // Otherwise, return an empty list
        return List.of();
    }

    @PostMapping
    public Contact createContact(@RequestBody ContactRequestDTO dto, HttpServletRequest request) {
        // Extract user ID from JWT token
        Long userId = getUserIdFromToken(request);
        return contactService.saveContact(dto, userId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Contact> updateContact(
            @PathVariable Long id,
            @RequestBody ContactRequestDTO dto,
            HttpServletRequest request) {
        // Extract user ID from JWT token
        Long userId = getUserIdFromToken(request);

        // Update the contact
        Contact updatedContact = contactService.updateContact(id, dto, userId);

        return ResponseEntity.ok(updatedContact);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(
            @PathVariable Long id,
            HttpServletRequest request) {
        // Extract user ID from JWT token
        Long userId = getUserIdFromToken(request);

        // Delete the contact
        contactService.deleteContact(id, userId);

        return ResponseEntity.noContent().build();
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
