package contacts.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class PhoneNumber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @com.fasterxml.jackson.annotation.JsonBackReference
    @JoinColumn(name = "contact_id")
    private Contact contact;

    @Column(nullable = false)
    private String phoneNumber;
}
