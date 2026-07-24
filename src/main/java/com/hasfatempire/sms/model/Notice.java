package com.hasfatempire.sms.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "notices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 4000)
    private String content;

    @Enumerated(EnumType.STRING)
    private Audience audience;

    private String postedBy;

    @Builder.Default
    private Instant postedAt = Instant.now();

    public enum Audience {
        ALL, TEACHERS, PARENTS, STUDENTS
    }
}
