package contacts.dto;

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
    private String email;
    private List<String> phoneNumbers;
    private List<AddressDTO> addresses;
    private String tajNumber;
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
