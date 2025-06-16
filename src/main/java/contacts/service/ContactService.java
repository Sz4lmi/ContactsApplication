package contacts.service;

import contacts.domain.Address;
import contacts.domain.Contact;
import contacts.domain.PhoneNumber;
import contacts.dto.ContactRequestDTO;
import contacts.repository.ContactRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactService {

    private final ContactRepository contactRepository;

    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    public List<Contact> getAllContacts() {
        return contactRepository.findAll();
    }

    public Contact saveContact(ContactRequestDTO dto) {
        Contact contact = new Contact();
        contact.setFirstName(dto.firstName);
        contact.setLastName(dto.lastName);
        contact.setEmail(dto.email);
        //contact.setUser(currentUser);

        // phone numbers
        if (dto.phoneNumbers != null) {
            for (String number : dto.phoneNumbers) {
                PhoneNumber phoneNumber = new PhoneNumber();
                phoneNumber.setPhoneNumber(number);
                phoneNumber.setContact(contact);
                contact.getPhoneNumbers().add(phoneNumber);
            }
        }

        // addresses
        if (dto.addresses != null) {
            for (ContactRequestDTO.AddressDTO a : dto.addresses) {
                Address address = new Address();
                address.setStreet(a.street);
                address.setCity(a.city);
                address.setZipCode(a.zipCode);
                address.setContact(contact);
                contact.getAddresses().add(address);
            }
        }

        return contactRepository.save(contact);
    }
}