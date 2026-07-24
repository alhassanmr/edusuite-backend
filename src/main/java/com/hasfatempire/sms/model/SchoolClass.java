package com.hasfatempire.sms.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "school_classes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "school_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private School school;

    @Column(nullable = false)
    private String name; // e.g. "Basic 6", "JHS 2"

    private String section; // e.g. "A", "B"

    @ManyToOne
    @JoinColumn(name = "homeroom_teacher_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Teacher homeroomTeacher;

    private String academicYear; // e.g. "2025/2026"
}
