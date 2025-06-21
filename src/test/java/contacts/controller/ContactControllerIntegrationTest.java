package contacts.controller;

import contacts.domain.Contact;
import contacts.domain.User;
import contacts.dto.ContactListDTO;
import contacts.dto.ContactRequestDTO;
import contacts.service.ContactService;
import contacts.util.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@ExtendWith(MockitoExtension.class)
public class ContactControllerIntegrationTest {

    private MockMvc mockMvc;

    @Mock
    private ContactService contactService;

    @InjectMocks
    private ContactController contactController;

    private ObjectMapper objectMapper;
    private Contact testContact;
    private User testUser;
    private ContactRequestDTO testContactDTO;
    private List<ContactListDTO> testContactListDTOs;

    @BeforeEach
    void setUp() {
        // Setup MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(contactController).build();

        // Setup ObjectMapper with JavaTimeModule for LocalDate serialization
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

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

        // Setup test contact list DTOs
        ContactListDTO contactListDTO = new ContactListDTO();
        contactListDTO.setId(1L);
        contactListDTO.setFirstName("John");
        contactListDTO.setLastName("Doe");
        contactListDTO.setEmail("john.doe@example.com");
        contactListDTO.setPhoneNumbers(new ArrayList<>());
        contactListDTO.setAddresses(new ArrayList<>());

        testContactListDTOs = Arrays.asList(contactListDTO);
    }

    @Test
    void getContactById_WhenContactExists_ShouldReturnContact() throws Exception {
        // Arrange
        when(contactService.getContactById(1L)).thenReturn(testContact);

        // Act & Assert
        try (MockedStatic<JwtUtils> jwtUtils = Mockito.mockStatic(JwtUtils.class)) {
            jwtUtils.when(() -> JwtUtils.getUserIdFromToken(any())).thenReturn(1L);
            jwtUtils.when(() -> JwtUtils.getRoleFromToken(any())).thenReturn("ROLE_USER");

            mockMvc.perform(get("/api/contacts/1")
                    .header("Authorization", "Bearer token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testContact.getId()))
                    .andExpect(jsonPath("$.firstName").value(testContact.getFirstName()))
                    .andExpect(jsonPath("$.lastName").value(testContact.getLastName()));
        }
    }

    @Test
    void getContactById_WhenContactDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(contactService.getContactById(1L)).thenReturn(null);

        // Act & Assert
        try (MockedStatic<JwtUtils> jwtUtils = Mockito.mockStatic(JwtUtils.class)) {
            jwtUtils.when(() -> JwtUtils.getUserIdFromToken(any())).thenReturn(1L);
            jwtUtils.when(() -> JwtUtils.getRoleFromToken(any())).thenReturn("ROLE_USER");

            mockMvc.perform(get("/api/contacts/1")
                    .header("Authorization", "Bearer token"))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    void getAllContacts_ShouldReturnContacts() throws Exception {
        // Arrange
        when(contactService.getContactListByUserId(any())).thenReturn(testContactListDTOs);

        // Act & Assert
        try (MockedStatic<JwtUtils> jwtUtils = Mockito.mockStatic(JwtUtils.class)) {
            jwtUtils.when(() -> JwtUtils.getUserIdFromToken(any())).thenReturn(1L);
            jwtUtils.when(() -> JwtUtils.getRoleFromToken(any())).thenReturn("ROLE_USER");

            mockMvc.perform(get("/api/contacts")
                    .header("Authorization", "Bearer token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(testContactListDTOs.get(0).getId()))
                    .andExpect(jsonPath("$[0].firstName").value(testContactListDTOs.get(0).getFirstName()))
                    .andExpect(jsonPath("$[0].lastName").value(testContactListDTOs.get(0).getLastName()));
        }
    }

    @Test
    void getContactList_AsAdmin_ShouldReturnAllContacts() throws Exception {
        // Arrange
        when(contactService.getAllContactsAsList()).thenReturn(testContactListDTOs);

        // Act & Assert
        try (MockedStatic<JwtUtils> jwtUtils = Mockito.mockStatic(JwtUtils.class)) {
            jwtUtils.when(() -> JwtUtils.getUserIdFromToken(any())).thenReturn(2L);
            jwtUtils.when(() -> JwtUtils.getRoleFromToken(any())).thenReturn("ROLE_ADMIN");

            mockMvc.perform(get("/api/contacts/list")
                    .header("Authorization", "Bearer adminToken"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(testContactListDTOs.get(0).getId()))
                    .andExpect(jsonPath("$[0].firstName").value(testContactListDTOs.get(0).getFirstName()))
                    .andExpect(jsonPath("$[0].lastName").value(testContactListDTOs.get(0).getLastName()));
        }
    }

    @Test
    void getContactList_AsUser_ShouldReturnUserContacts() throws Exception {
        // Arrange
        when(contactService.getContactListByUserId(any())).thenReturn(testContactListDTOs);

        // Act & Assert
        try (MockedStatic<JwtUtils> jwtUtils = Mockito.mockStatic(JwtUtils.class)) {
            jwtUtils.when(() -> JwtUtils.getUserIdFromToken(any())).thenReturn(1L);
            jwtUtils.when(() -> JwtUtils.getRoleFromToken(any())).thenReturn("ROLE_USER");

            mockMvc.perform(get("/api/contacts/list")
                    .header("Authorization", "Bearer userToken"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(testContactListDTOs.get(0).getId()))
                    .andExpect(jsonPath("$[0].firstName").value(testContactListDTOs.get(0).getFirstName()))
                    .andExpect(jsonPath("$[0].lastName").value(testContactListDTOs.get(0).getLastName()));
        }
    }

    @Test
    void createContact_WithValidData_ShouldCreateContact() throws Exception {
        // Arrange
        when(contactService.saveContact(any(ContactRequestDTO.class), any())).thenReturn(testContact);

        // Act & Assert
        try (MockedStatic<JwtUtils> jwtUtils = Mockito.mockStatic(JwtUtils.class)) {
            jwtUtils.when(() -> JwtUtils.getUserIdFromToken(any())).thenReturn(1L);
            jwtUtils.when(() -> JwtUtils.getRoleFromToken(any())).thenReturn("ROLE_USER");

            mockMvc.perform(post("/api/contacts")
                    .header("Authorization", "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testContactDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testContact.getId()))
                    .andExpect(jsonPath("$.firstName").value(testContact.getFirstName()))
                    .andExpect(jsonPath("$.lastName").value(testContact.getLastName()));
        }
    }

    @Test
    void updateContact_WithValidData_ShouldUpdateContact() throws Exception {
        // Arrange
        when(contactService.updateContact(eq(1L), any(ContactRequestDTO.class), any())).thenReturn(testContact);

        // Act & Assert
        try (MockedStatic<JwtUtils> jwtUtils = Mockito.mockStatic(JwtUtils.class)) {
            jwtUtils.when(() -> JwtUtils.getUserIdFromToken(any())).thenReturn(1L);
            jwtUtils.when(() -> JwtUtils.getRoleFromToken(any())).thenReturn("ROLE_USER");

            mockMvc.perform(put("/api/contacts/1")
                    .header("Authorization", "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testContactDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testContact.getId()))
                    .andExpect(jsonPath("$.firstName").value(testContact.getFirstName()))
                    .andExpect(jsonPath("$.lastName").value(testContact.getLastName()));
        }
    }

    @Test
    void deleteContact_ShouldDeleteContact() throws Exception {
        // Act & Assert
        try (MockedStatic<JwtUtils> jwtUtils = Mockito.mockStatic(JwtUtils.class)) {
            jwtUtils.when(() -> JwtUtils.getUserIdFromToken(any())).thenReturn(1L);
            jwtUtils.when(() -> JwtUtils.getRoleFromToken(any())).thenReturn("ROLE_USER");

            mockMvc.perform(delete("/api/contacts/1")
                    .header("Authorization", "Bearer token"))
                    .andExpect(status().isNoContent());
        }
    }
}
