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
        contact.setFirstName(dto.getFirstName());
        contact.setLastName(dto.getLastName());
        contact.setEmail(dto.getEmail());
        contact.setTajNumber(dto.getTajNumber());
        contact.setTaxId(dto.getTaxId());
        contact.setMotherName(dto.getMotherName());
        contact.setBirthDate(dto.getBirthDate());
        //contact.setUser(currentUser);

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