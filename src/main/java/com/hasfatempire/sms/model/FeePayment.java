package com.hasfatempire.sms.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fee_payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    @JsonIgnoreProperties({"payments", "hibernateLazyInitializer", "handler"})
    private FeeInvoice invoice;

    private BigDecimal amount;

    private LocalDate paymentDate;

    private String method; // CASH, MOBILE_MONEY, BANK_TRANSFER, CARD

    private String reference;

    private String receivedBy;
}
