package contacts.service;

import contacts.domain.Address;
import contacts.domain.Contact;
import contacts.domain.PhoneNumber;
import contacts.domain.User;
import contacts.dto.ContactRequestDTO;
import contacts.repository.ContactRepository;
import contacts.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<Contact> getContactsByUserId(Long userId) {
        // Find the user by ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Return the user's contacts
        return user.getContacts();
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
}
