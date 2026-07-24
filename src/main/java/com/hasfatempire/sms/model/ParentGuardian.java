package com.hasfatempire.sms.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "parents_guardians")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParentGuardian {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String occupation;
    private String address;
    private String relationship; // Mother, Father, Guardian
}
