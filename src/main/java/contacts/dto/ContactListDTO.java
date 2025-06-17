package contacts.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactListDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private List<AddressDTO> addresses;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressDTO {
        private String street;
        private String city;
        private String zipCode;
    }
}