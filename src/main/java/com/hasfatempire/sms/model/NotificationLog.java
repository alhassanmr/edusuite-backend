package com.hasfatempire.sms.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "notification_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "school_id")
    private School school;

    @Enumerated(EnumType.STRING)
    private Channel channel; // SMS, EMAIL

    private String recipient; // phone or email

    private String subject;

    @Column(length = 2000)
    private String message;

    @Enumerated(EnumType.STRING)
    private Status status; // PENDING, SENT, FAILED

    private String providerResponse;

    @Builder.Default
    private Instant createdAt = Instant.now();

    public enum Channel { SMS, EMAIL }
    public enum Status { PENDING, SENT, FAILED }
}
