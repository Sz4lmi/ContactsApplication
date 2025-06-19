package contacts.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for validating the ContactRequestDTO.
 * This test directly uses the Jakarta Bean Validation API to test the validation annotations.
 */
public class ContactRequestDTOValidationTest {

    private Validator validator;
    private ContactRequestDTO validDto;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        // Create a valid DTO for testing
        validDto = new ContactRequestDTO();
        validDto.setFirstName("John");
        validDto.setLastName("Doe");
        validDto.setEmail("john.doe@example.com");
        validDto.setPhoneNumbers(Arrays.asList("+36 30 123 4567"));
        validDto.setTajNumber("123456789");
        validDto.setTaxId("ABC123456");
    }

    @Test
    public void testValidDto() {
        Set<ConstraintViolation<ContactRequestDTO>> violations = validator.validate(validDto);
        assertTrue(violations.isEmpty(), "Valid DTO should not have validation errors");
    }

    @Test
    public void testInvalidEmail() {
        // Test email without @ character
        validDto.setEmail("johndoeexample.com");
        Set<ConstraintViolation<ContactRequestDTO>> violations = validator.validate(validDto);

        assertFalse(violations.isEmpty(), "Invalid email should have validation errors");
        boolean hasEmailError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email") && 
                               (v.getMessage().contains("Please provide a valid email address") || 
                                v.getMessage().contains("Email should be valid")));
        assertTrue(hasEmailError, "Should have error about invalid email format");
    }

    @Test
    public void testInvalidPhoneNumber() {
        // Test invalid phone number format
        validDto.setPhoneNumbers(Arrays.asList("12345678"));
        Set<ConstraintViolation<ContactRequestDTO>> violations = validator.validate(validDto);

        assertFalse(violations.isEmpty(), "Invalid phone number should have validation errors");
        boolean hasPhoneError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().contains("phoneNumbers") && 
                               v.getMessage().contains("Phone number should be either 11 numbers"));
        assertTrue(hasPhoneError, "Should have error about phone number format");
    }

    @Test
    public void testValidPhoneNumberFormats() {
        // Test all valid phone number formats
        String[] validFormats = {
            "+36303159270",    // + sign followed by 11 digits without spaces
            "06303159270",     // 11 digits without spaces
            "+36 30 315 9270", // + sign followed by 11 digits with spaces
            "06 30 315 9270"   // 11 digits with spaces
        };

        for (String format : validFormats) {
            validDto.setPhoneNumbers(Arrays.asList(format));
            Set<ConstraintViolation<ContactRequestDTO>> violations = validator.validate(validDto);
            assertTrue(violations.isEmpty(), "Phone number format '" + format + "' should be valid");
        }
    }

    @Test
    public void testInvalidTajNumber() {
        // Test TAJ number that's not 9 digits
        validDto.setTajNumber("12345");
        Set<ConstraintViolation<ContactRequestDTO>> violations = validator.validate(validDto);

        assertFalse(violations.isEmpty(), "Invalid TAJ number should have validation errors");
        boolean hasTajError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("tajNumber") && 
                               v.getMessage().contains("TAJ number should be exactly 9 digits"));
        assertTrue(hasTajError, "Should have error about TAJ number length");
    }

    @Test
    public void testInvalidTaxId_NoLetters() {
        // Test tax ID with no letters
        validDto.setTaxId("123456");
        Set<ConstraintViolation<ContactRequestDTO>> violations = validator.validate(validDto);

        assertFalse(violations.isEmpty(), "Tax ID with no letters should have validation errors");
        boolean hasTaxIdError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("taxId") && 
                               v.getMessage().contains("Tax ID should contain at least one letter"));
        assertTrue(hasTaxIdError, "Should have error about missing letters in tax ID");
    }

    @Test
    public void testInvalidTaxId_NoNumbers() {
        // Test tax ID with no numbers
        validDto.setTaxId("ABCDEF");
        Set<ConstraintViolation<ContactRequestDTO>> violations = validator.validate(validDto);

        assertFalse(violations.isEmpty(), "Tax ID with no numbers should have validation errors");
        boolean hasTaxIdError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("taxId") && 
                               v.getMessage().contains("Tax ID should contain at least one number"));
        assertTrue(hasTaxIdError, "Should have error about missing numbers in tax ID");
    }

    @Test
    public void testInvalidTaxId_TooShort() {
        // Test tax ID that's too short
        validDto.setTaxId("A1");
        Set<ConstraintViolation<ContactRequestDTO>> violations = validator.validate(validDto);

        assertFalse(violations.isEmpty(), "Tax ID that's too short should have validation errors");
        boolean hasTaxIdError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("taxId") && 
                               v.getMessage().contains("Tax ID should be at least 6 characters"));
        assertTrue(hasTaxIdError, "Should have error about tax ID being too short");
    }
}
