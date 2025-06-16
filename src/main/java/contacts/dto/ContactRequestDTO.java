package contacts.dto;

import java.util.List;

public class ContactRequestDTO {
    public String firstName;
    public String lastName;
    public String email;
    public List<String> phoneNumbers;
    public List<AddressDTO> addresses;

    public static class AddressDTO {
        public String street;
        public String city;
        public String zipCode;
    }
}