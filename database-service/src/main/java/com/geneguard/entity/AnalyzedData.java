package com.geneguard.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "analyzed_data")
public class AnalyzedData {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "analyzed_data_id", nullable = false, unique = true)
    private UUID analyzedDataId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "genetic_data_id", nullable = false)
    private GeneticData geneticData;

    @Column(name = "analyzed_result", nullable = false)
    private String analyzedResult;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
