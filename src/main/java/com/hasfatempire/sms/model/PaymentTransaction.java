package com.hasfatempire.sms.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String reference; // Paystack reference

    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    @JsonIgnoreProperties({"payments", "hibernateLazyInitializer", "handler"})
    private FeeInvoice invoice;

    @ManyToOne
    @JoinColumn(name = "school_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private School school;

    private BigDecimal amount;

    private String currency; // GHS, NGN, USD...

    @Enumerated(EnumType.STRING)
    private Status status; // INITIALIZED, SUCCESS, FAILED

    private String channel; // card, mobile_money, bank...

    private String initiatedByUsername;

    @Builder.Default
    private Instant createdAt = Instant.now();

    private Instant verifiedAt;

    public enum Status { INITIALIZED, SUCCESS, FAILED }
}
