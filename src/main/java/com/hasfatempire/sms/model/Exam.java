package com.hasfatempire.sms.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "exams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "school_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private School school;

    private String name; // "Mid-Term Exams", "End of Term 2"

    private String term;

    private String academicYear;

    @ManyToOne
    @JoinColumn(name = "school_class_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private SchoolClass schoolClass;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Subject subject;

    private LocalDate examDate;

    @Builder.Default
    private Double maxScore = 100.0;

    private Boolean published = false;
}
