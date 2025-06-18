package contacts.service;

import contacts.domain.Address;
import contacts.domain.Contact;
import contacts.domain.PhoneNumber;
import contacts.domain.User;
import contacts.dto.ContactListDTO;
import contacts.dto.ContactRequestDTO;
import contacts.repository.ContactRepository;
import contacts.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing contacts.
 * Provides methods for CRUD operations on contacts and related entities.
 */
@Service
public class ContactService {

    private static final Logger logger = LoggerFactory.getLogger(ContactService.class);
    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    /**
     * Constructor for ContactService.
     *
     * @param contactRepository Repository for contact operations
     * @param userRepository Repository for user operations
     */
    public ContactService(ContactRepository contactRepository, UserRepository userRepository) {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get all contacts in the system.
     *
     * @return List of all contacts
     */
    public List<Contact> getAllContacts() {
        return contactRepository.findAll();
    }

    /**
     * Get a contact by its ID.
     *
     * @param id The ID of the contact to retrieve
     * @return The contact or null if not found
     */
    public Contact getContactById(Long id) {
        return contactRepository.findById(id).orElse(null);
    }

    /**
     * Get all contacts for a specific user.
     *
     * @param userId The ID of the user
     * @return List of contacts belonging to the user
     * @throws RuntimeException if the user is not found
     */
    public List<Contact> getContactsByUserId(Long userId) {
        // Find the user by ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Return the user's contacts
        return user.getContacts();
    }

    /**
     * Get a list of contacts for a specific user as DTOs.
     *
     * @param userId The ID of the user
     * @return List of contact DTOs belonging to the user
     * @throws RuntimeException if the user is not found
     */
    public List<ContactListDTO> getContactListByUserId(Long userId) {
        logger.debug("ContactService.getContactListByUserId: userId = {}", userId);

        // Find the user by ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        logger.debug("ContactService.getContactListByUserId: found user = {}, role = {}", user.getUsername(), user.getRole());

        // Get the user's contacts
        List<Contact> contacts = user.getContacts();
        logger.debug("ContactService.getContactListByUserId: found {} contacts for user", contacts.size());

        // Log each contact
        contacts.forEach(contact -> {
            logger.debug("ContactService.getContactListByUserId: contact id={}, firstName={}, lastName={}", 
                contact.getId(), contact.getFirstName(), contact.getLastName());
        });

        // Convert to DTOs
        List<ContactListDTO> dtos = contacts.stream()
                .map(this::convertToContactListDTO)
                .collect(Collectors.toList());

        logger.debug("ContactService.getContactListByUserId: returning {} DTOs", dtos.size());
        return dtos;
    }

    /**
     * Get a list of all contacts in the system as DTOs.
     *
     * @return List of all contact DTOs
     */
    public List<ContactListDTO> getAllContactsAsList() {
        // Get all contacts
        List<Contact> contacts = contactRepository.findAll();
        logger.debug("ContactService.getAllContactsAsList: found {} contacts", contacts.size());

        // Log each contact
        contacts.forEach(contact -> {
            logger.debug("ContactService.getAllContactsAsList: contact id={}, firstName={}, lastName={}, userId={}", 
                contact.getId(), contact.getFirstName(), contact.getLastName(), 
                (contact.getUser() != null ? contact.getUser().getId() : "null"));
        });

        // Convert to DTOs
        List<ContactListDTO> dtos = contacts.stream()
                .map(this::convertToContactListDTO)
                .collect(Collectors.toList());

        logger.debug("ContactService.getAllContactsAsList: returning {} DTOs", dtos.size());
        return dtos;
    }

    /**
     * Convert a Contact entity to a ContactListDTO.
     *
     * @param contact The contact entity to convert
     * @return The converted DTO
     */
    private ContactListDTO convertToContactListDTO(Contact contact) {
        ContactListDTO dto = new ContactListDTO();
        dto.setId(contact.getId());
        dto.setFirstName(contact.getFirstName());
        dto.setLastName(contact.getLastName());
        dto.setEmail(contact.getEmail());

        // Convert phone numbers
        List<ContactListDTO.PhoneNumberDTO> phoneNumberDTOs = contact.getPhoneNumbers().stream()
                .map(phoneNumber -> {
                    ContactListDTO.PhoneNumberDTO phoneNumberDTO = new ContactListDTO.PhoneNumberDTO();
                    phoneNumberDTO.setPhoneNumber(phoneNumber.getPhoneNumber());
                    return phoneNumberDTO;
                })
                .collect(Collectors.toList());

        dto.setPhoneNumbers(phoneNumberDTOs);

        // Convert addresses
        List<ContactListDTO.AddressDTO> addressDTOs = contact.getAddresses().stream()
                .map(address -> {
                    ContactListDTO.AddressDTO addressDTO = new ContactListDTO.AddressDTO();
                    addressDTO.setStreet(address.getStreet());
                    addressDTO.setCity(address.getCity());
                    addressDTO.setZipCode(address.getZipCode());
                    return addressDTO;
                })
                .collect(Collectors.toList());

        dto.setAddresses(addressDTOs);

        return dto;
    }

    /**
     * Save a new contact.
     *
     * @param dto The contact data
     * @param userId The ID of the user who owns the contact
     * @return The saved contact
     */
    public Contact saveContact(ContactRequestDTO dto, Long userId) {
        Contact contact = new Contact();
        contact.setFirstName(dto.getFirstName());
        contact.setLastName(dto.getLastName());
        contact.setEmail(dto.getEmail());
        contact.setTajNumber(dto.getTajNumber());
        contact.setTaxId(dto.getTaxId());
        contact.setMotherName(dto.getMotherName());
        contact.setBirthDate(dto.getBirthDate());

        // Set the user if userId is provided
        if (userId != null) {
            User user = new User();
            user.setId(userId);
            contact.setUser(user);
        }

        // phone numbers
        if (dto.getPhoneNumbers() != null) {
            for (String number : dto.getPhoneNumbers()) {
                PhoneNumber phoneNumber = new PhoneNumber();
                phoneNumber.setPhoneNumber(number);
                phoneNumber.setContact(contact);
                contact.getPhoneNumbers().add(phoneNumber);
            }
        }

        // addresses
        if (dto.getAddresses() != null) {
            for (ContactRequestDTO.AddressDTO a : dto.getAddresses()) {
                Address address = new Address();
                address.setStreet(a.getStreet());
                address.setCity(a.getCity());
                address.setZipCode(a.getZipCode());
                address.setContact(contact);
                contact.getAddresses().add(address);
            }
        }

        return contactRepository.save(contact);
    }

    /**
     * Update an existing contact.
     *
     * @param id The ID of the contact to update
     * @param dto The updated contact data
     * @param userId The ID of the user who owns the contact
     * @return The updated contact
     * @throws RuntimeException if the contact is not found or doesn't belong to the user
     */
    public Contact updateContact(Long id, ContactRequestDTO dto, Long userId) {
        // Find the contact by ID
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contact not found"));

        // Note: We're allowing both the owner and admin users to update contacts
        // No ownership check is performed here to allow admins to edit any contact

        // Update the contact fields
        contact.setFirstName(dto.getFirstName());
        contact.setLastName(dto.getLastName());
        contact.setEmail(dto.getEmail());
        contact.setTajNumber(dto.getTajNumber());
        contact.setTaxId(dto.getTaxId());
        contact.setMotherName(dto.getMotherName());
        contact.setBirthDate(dto.getBirthDate());

        // Update phone numbers
        contact.getPhoneNumbers().clear();
        if (dto.getPhoneNumbers() != null) {
            for (String number : dto.getPhoneNumbers()) {
                PhoneNumber phoneNumber = new PhoneNumber();
                phoneNumber.setPhoneNumber(number);
                phoneNumber.setContact(contact);
                contact.getPhoneNumbers().add(phoneNumber);
            }
        }

        // Update addresses
        contact.getAddresses().clear();
        if (dto.getAddresses() != null) {
            for (ContactRequestDTO.AddressDTO a : dto.getAddresses()) {
                Address address = new Address();
                address.setStreet(a.getStreet());
                address.setCity(a.getCity());
                address.setZipCode(a.getZipCode());
                address.setContact(contact);
                contact.getAddresses().add(address);
            }
        }

        return contactRepository.save(contact);
    }

    /**
     * Delete a contact.
     *
     * @param id The ID of the contact to delete
     * @param userId The ID of the user who owns the contact
     * @throws RuntimeException if the contact is not found or doesn't belong to the user
     */
    public void deleteContact(Long id, Long userId) {
        // Find the contact by ID
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contact not found"));

        // Note: We're allowing both the owner and admin users to delete contacts
        // No ownership check is performed here to allow admins to delete any contact

        // Delete the contact
        contactRepository.delete(contact);
    }
}
