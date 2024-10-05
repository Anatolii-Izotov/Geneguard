package com.geneguard.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "genetic_data")

public class GeneticData {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "genetic_data_id", nullable = false, unique = true)
    private UUID geneticDataId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String dataContent; // JSON

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
