package contacts.dto;

import contacts.validation.PhoneNumberList;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactRequestDTO {
    private String firstName;
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Pattern(regexp = ".*@.*", message = "Please provide a valid email address.")
    private String email;

    @Valid
    @PhoneNumberList(message = "Please enter a valid phone number")
    private List<String> phoneNumbers;

    @Valid
    private List<AddressDTO> addresses;

    @NotBlank(message = "TAJ number is required")
    @Pattern(regexp = "^\\d{9}$", message = "TAJ number should be exactly 9 digits.")
    private String tajNumber;

    @NotBlank(message = "Tax ID is required")
    @Pattern(regexp = "^\\d{10}$", message = "Tax ID should be exactly 10 digits.")
    private String taxId;

    private String motherName;
    private LocalDate birthDate;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressDTO {
        private String street;
        private String city;
        private String zipCode;
    }
}
