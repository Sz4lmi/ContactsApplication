package contacts.service;

import contacts.domain.Address;
import contacts.domain.Contact;
import contacts.domain.PhoneNumber;
import contacts.domain.User;
import contacts.dto.ContactListDTO;
import contacts.dto.ContactRequestDTO;
import contacts.repository.ContactRepository;
import contacts.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContactService {

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    public ContactService(ContactRepository contactRepository, UserRepository userRepository) {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
    }

    public List<Contact> getAllContacts() {
        return contactRepository.findAll();
    }

    public Contact getContactById(Long id) {
        return contactRepository.findById(id).orElse(null);
    }

    public List<Contact> getContactsByUserId(Long userId) {
        // Find the user by ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Return the user's contacts
        return user.getContacts();
    }

    public List<ContactListDTO> getContactListByUserId(Long userId) {
        // Find the user by ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get the user's contacts
        List<Contact> contacts = user.getContacts();

        // Convert to DTOs
        return contacts.stream()
                .map(this::convertToContactListDTO)
                .collect(Collectors.toList());
    }

    public List<ContactListDTO> getAllContactsAsList() {
        // Get all contacts
        List<Contact> contacts = contactRepository.findAll();

        // Convert to DTOs
        return contacts.stream()
                .map(this::convertToContactListDTO)
                .collect(Collectors.toList());
    }

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

    public Contact updateContact(Long id, ContactRequestDTO dto, Long userId) {
        // Find the contact by ID
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contact not found"));

        // Verify that the contact belongs to the user
        if (userId != null && contact.getUser() != null && !contact.getUser().getId().equals(userId)) {
            throw new RuntimeException("Contact does not belong to the current user");
        }

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

    public void deleteContact(Long id, Long userId) {
        // Find the contact by ID
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contact not found"));

        // Verify that the contact belongs to the user or user is admin
        if (userId != null && contact.getUser() != null && !contact.getUser().getId().equals(userId)) {
            throw new RuntimeException("Contact does not belong to the current user");
        }

        // Delete the contact
        contactRepository.delete(contact);
    }
}
