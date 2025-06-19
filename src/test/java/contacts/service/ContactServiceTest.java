package contacts.service;

import contacts.domain.Address;
import contacts.domain.Contact;
import contacts.domain.PhoneNumber;
import contacts.domain.User;
import contacts.dto.ContactListDTO;
import contacts.dto.ContactRequestDTO;
import contacts.repository.ContactRepository;
import contacts.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ContactService contactService;

    private User testUser;
    private Contact testContact;
    private ContactRequestDTO testContactDTO;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("hashedpassword");
        testUser.setRole("ROLE_USER");
        testUser.setContacts(new ArrayList<>());

        // Setup test contact
        testContact = new Contact();
        testContact.setId(1L);
        testContact.setFirstName("John");
        testContact.setLastName("Doe");
        testContact.setEmail("john.doe@example.com");
        testContact.setTajNumber("123456789");
        testContact.setTaxId("1234567890");
        testContact.setMotherName("Jane Doe");
        testContact.setBirthDate(LocalDate.of(1990, 1, 1));
        testContact.setUser(testUser);
        testContact.setPhoneNumbers(new ArrayList<>());
        testContact.setAddresses(new ArrayList<>());

        // Add phone number to contact
        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setId(1L);
        phoneNumber.setPhoneNumber("1234567890");
        phoneNumber.setContact(testContact);
        testContact.getPhoneNumbers().add(phoneNumber);

        // Add address to contact
        Address address = new Address();
        address.setId(1L);
        address.setStreet("123 Main St");
        address.setCity("Anytown");
        address.setZipCode("12345");
        address.setContact(testContact);
        testContact.getAddresses().add(address);

        // Add contact to user
        testUser.getContacts().add(testContact);

        // Setup test contact DTO
        testContactDTO = new ContactRequestDTO();
        testContactDTO.setFirstName("John");
        testContactDTO.setLastName("Doe");
        testContactDTO.setEmail("john.doe@example.com");
        testContactDTO.setTajNumber("123456789");
        testContactDTO.setTaxId("1234567890");
        testContactDTO.setMotherName("Jane Doe");
        testContactDTO.setBirthDate(LocalDate.of(1990, 1, 1));
        testContactDTO.setPhoneNumbers(Arrays.asList("1234567890"));
        
        ContactRequestDTO.AddressDTO addressDTO = new ContactRequestDTO.AddressDTO();
        addressDTO.setStreet("123 Main St");
        addressDTO.setCity("Anytown");
        addressDTO.setZipCode("12345");
        testContactDTO.setAddresses(Arrays.asList(addressDTO));
    }

    @Test
    void getAllContacts_ShouldReturnAllContacts() {
        // Arrange
        List<Contact> contacts = Arrays.asList(testContact);
        when(contactRepository.findAll()).thenReturn(contacts);

        // Act
        List<Contact> result = contactService.getAllContacts();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testContact.getFirstName(), result.get(0).getFirstName());
        verify(contactRepository, times(1)).findAll();
    }

    @Test
    void getContactById_WhenContactExists_ShouldReturnContact() {
        // Arrange
        when(contactRepository.findById(1L)).thenReturn(Optional.of(testContact));

        // Act
        Contact result = contactService.getContactById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testContact.getFirstName(), result.getFirstName());
        verify(contactRepository, times(1)).findById(1L);
    }

    @Test
    void getContactById_WhenContactDoesNotExist_ShouldReturnNull() {
        // Arrange
        when(contactRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Contact result = contactService.getContactById(1L);

        // Assert
        assertNull(result);
        verify(contactRepository, times(1)).findById(1L);
    }

    @Test
    void getContactsByUserId_WhenUserExists_ShouldReturnUserContacts() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        List<Contact> result = contactService.getContactsByUserId(1L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testContact.getFirstName(), result.get(0).getFirstName());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getContactsByUserId_WhenUserDoesNotExist_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            contactService.getContactsByUserId(1L);
        });
        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getContactListByUserId_WhenUserExists_ShouldReturnContactDTOs() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        List<ContactListDTO> result = contactService.getContactListByUserId(1L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testContact.getFirstName(), result.get(0).getFirstName());
        assertEquals(testContact.getLastName(), result.get(0).getLastName());
        assertEquals(testContact.getEmail(), result.get(0).getEmail());
        assertEquals(1, result.get(0).getPhoneNumbers().size());
        assertEquals(testContact.getPhoneNumbers().get(0).getPhoneNumber(), 
                     result.get(0).getPhoneNumbers().get(0).getPhoneNumber());
        assertEquals(1, result.get(0).getAddresses().size());
        assertEquals(testContact.getAddresses().get(0).getStreet(), 
                     result.get(0).getAddresses().get(0).getStreet());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getContactListByUserId_WhenUserDoesNotExist_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            contactService.getContactListByUserId(1L);
        });
        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getAllContactsAsList_ShouldReturnAllContactsAsDTOs() {
        // Arrange
        List<Contact> contacts = Arrays.asList(testContact);
        when(contactRepository.findAll()).thenReturn(contacts);

        // Act
        List<ContactListDTO> result = contactService.getAllContactsAsList();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testContact.getFirstName(), result.get(0).getFirstName());
        assertEquals(testContact.getLastName(), result.get(0).getLastName());
        assertEquals(testContact.getEmail(), result.get(0).getEmail());
        verify(contactRepository, times(1)).findAll();
    }

    @Test
    void saveContact_WithValidData_ShouldSaveContact() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(contactRepository.save(any(Contact.class))).thenReturn(testContact);

        // Act
        Contact result = contactService.saveContact(testContactDTO, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(testContact.getFirstName(), result.getFirstName());
        assertEquals(testContact.getLastName(), result.getLastName());
        verify(userRepository, times(1)).findById(1L);
        verify(contactRepository, times(1)).save(any(Contact.class));
    }

    @Test
    void saveContact_WithNonExistentUser_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            contactService.saveContact(testContactDTO, 1L);
        });
        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findById(1L);
        verify(contactRepository, never()).save(any(Contact.class));
    }

    @Test
    void updateContact_WithValidData_ShouldUpdateContact() {
        // Arrange
        when(contactRepository.findById(1L)).thenReturn(Optional.of(testContact));
        when(contactRepository.save(any(Contact.class))).thenReturn(testContact);

        // Modify the DTO to simulate an update
        testContactDTO.setFirstName("Jane");
        testContactDTO.setLastName("Smith");

        // Act
        Contact result = contactService.updateContact(1L, testContactDTO, 1L);

        // Assert
        assertNotNull(result);
        verify(contactRepository, times(1)).findById(1L);
        verify(contactRepository, times(1)).save(any(Contact.class));
    }

    @Test
    void updateContact_WithNonExistentContact_ShouldThrowException() {
        // Arrange
        when(contactRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            contactService.updateContact(1L, testContactDTO, 1L);
        });
        assertEquals("Contact not found", exception.getMessage());
        verify(contactRepository, times(1)).findById(1L);
        verify(contactRepository, never()).save(any(Contact.class));
    }

    @Test
    void deleteContact_WithExistingContact_ShouldDeleteContact() {
        // Arrange
        when(contactRepository.findById(1L)).thenReturn(Optional.of(testContact));
        doNothing().when(contactRepository).delete(testContact);

        // Act
        contactService.deleteContact(1L, 1L);

        // Assert
        verify(contactRepository, times(1)).findById(1L);
        verify(contactRepository, times(1)).delete(testContact);
    }

    @Test
    void deleteContact_WithNonExistentContact_ShouldThrowException() {
        // Arrange
        when(contactRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            contactService.deleteContact(1L, 1L);
        });
        assertEquals("Contact not found", exception.getMessage());
        verify(contactRepository, times(1)).findById(1L);
        verify(contactRepository, never()).delete(any(Contact.class));
    }
}