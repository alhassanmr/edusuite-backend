package com.hasfatempire.sms.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "schools")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank
    private String name; // "St. Mary's Primary School"

    @Column(nullable = false, unique = true)
    private String slug; // "st-marys-primary" for URL-friendly identifier

    @Email
    @NotBlank
    private String contactEmail;

    private String phone;

    private String address;

    private String city;

    private String country;

    private String logoUrl;

    private String theme; // branding color hex

    @Column(nullable = false)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, SUSPENDED, ARCHIVED

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    private Instant updatedAt;

    @Column(nullable = false)
    private String createdByUsername; // admin who registered the school

    // ---- Payment settlement (Paystack split payments) ----
    // Money goes DIRECTLY to the school's account; platform takes a small commission.
    private String paystackSubaccountCode;   // e.g. ACCT_xxxxx (set once settlement is configured)
    private String settlementBankCode;       // Paystack bank/MoMo code
    private String settlementBankName;       // e.g. "GCB Bank" or "MTN Mobile Money"
    private String settlementAccountNumber;  // bank account or MoMo number
    private String settlementAccountName;    // must match account holder

    /** Percentage of each payment retained by the platform (Hasfat Empire). */
    @Builder.Default
    private Double platformFeePercent = 1.0;
}
