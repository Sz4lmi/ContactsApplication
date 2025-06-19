package contacts.controller;

import contacts.domain.Contact;
import contacts.dto.ContactListDTO;
import contacts.dto.ContactRequestDTO;
import contacts.service.ContactService;
import contacts.util.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

import contacts.config.SecurityConstants;

/**
 * REST controller for managing contacts.
 * Provides endpoints for CRUD operations on contacts.
 */
@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);
    private final ContactService contactService;

    /**
     * Constructor for ContactController.
     *
     * @param contactService The service for contact operations
     */
    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    /**
     * Get a contact by its ID.
     * Only returns the contact if the user has permission to view it.
     *
     * @param id The ID of the contact to retrieve
     * @param request The HTTP request containing authentication information
     * @return The contact or 404 if not found or not accessible
     */
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

    /**
     * Get all contacts accessible to the user.
     * If user ID is available, returns only contacts for that user.
     * Otherwise, returns all contacts.
     *
     * @param request The HTTP request containing authentication information
     * @return List of contacts
     */
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

    /**
     * Get a list of contacts based on user role and ID.
     * Admin users can see all contacts, regular users see only their contacts.
     *
     * @param request The HTTP request containing authentication information
     * @return List of contacts as DTOs
     */
    @GetMapping("/list")
    public List<ContactListDTO> getContactList(HttpServletRequest request) {
        // Extract user ID and role from JWT token
        Long userId = getUserIdFromToken(request);
        String role = getRoleFromToken(request);

        logger.debug("ContactController.getContactList: userId = {}, role = {}", userId, role);

        // If user is admin, return all contacts
        if (role != null && role.equals("ROLE_ADMIN")) {
            List<ContactListDTO> allContacts = contactService.getAllContactsAsList();
            logger.debug("ContactController.getContactList: returning all contacts, count = {}", allContacts.size());
            return allContacts;
        }

        // If we have a userId, get contacts for that user
        if (userId != null) {
            List<ContactListDTO> userContacts = contactService.getContactListByUserId(userId);
            logger.debug("ContactController.getContactList: returning user contacts, count = {}", userContacts.size());
            return userContacts;
        }

        // Otherwise, return an empty list
        logger.debug("ContactController.getContactList: returning empty list");
        return List.of();
    }

    /**
     * Create a new contact for the authenticated user.
     *
     * @param dto The contact data
     * @param request The HTTP request containing authentication information
     * @return The created contact
     */
    @PostMapping
    public ResponseEntity<?> createContact(@Valid @RequestBody ContactRequestDTO dto, HttpServletRequest request) {
        // Extract user ID from JWT token
        Long userId = getUserIdFromToken(request);
        Contact contact = contactService.saveContact(dto, userId);
        return ResponseEntity.ok(contact);
    }

    /**
     * Update an existing contact.
     * Both regular users and admins can update contacts.
     *
     * @param id The ID of the contact to update
     * @param dto The updated contact data
     * @param request The HTTP request containing authentication information
     * @return The updated contact or appropriate error response
     */
    @PutMapping("/{id}")
    public ResponseEntity<Contact> updateContact(
            @PathVariable Long id,
            @Valid @RequestBody ContactRequestDTO dto,
            HttpServletRequest request) {
        // Extract user ID and role from JWT token
        Long userId = getUserIdFromToken(request);
        String role = getRoleFromToken(request);

        // Update the contact
        Contact updatedContact = contactService.updateContact(id, dto, userId);

        return ResponseEntity.ok(updatedContact);
    }

    /**
     * Delete a contact.
     * Both regular users and admins can delete contacts.
     *
     * @param id The ID of the contact to delete
     * @param request The HTTP request containing authentication information
     * @return 204 No Content on success, or appropriate error response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(
            @PathVariable Long id,
            HttpServletRequest request) {
        // Extract user ID and role from JWT token
        Long userId = getUserIdFromToken(request);
        String role = getRoleFromToken(request);

        // Delete the contact
        contactService.deleteContact(id, userId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Extracts the user ID from the JWT token in the request.
     *
     * @param request The HTTP request containing the JWT token
     * @return The user ID or null if not found or token is invalid
     */
    private Long getUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        logger.debug("ContactController.getUserIdFromToken: authHeader = {}", 
            (authHeader != null ? authHeader.substring(0, Math.min(20, authHeader.length())) + "..." : "null"));

        Long userId = JwtUtils.getUserIdFromToken(request);
        logger.debug("ContactController.getUserIdFromToken: extracted userId = {}", userId);
        return userId;
    }

    /**
     * Extracts the user role from the JWT token in the request.
     *
     * @param request The HTTP request containing the JWT token
     * @return The user role or null if not found or token is invalid
     */
    private String getRoleFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        logger.debug("ContactController.getRoleFromToken: authHeader = {}", 
            (authHeader != null ? authHeader.substring(0, Math.min(20, authHeader.length())) + "..." : "null"));

        String role = JwtUtils.getRoleFromToken(request);
        logger.debug("ContactController.getRoleFromToken: extracted role = {}", role);
        return role;
    }
}
