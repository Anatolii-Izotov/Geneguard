package com.genguard.entity;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "vcf_data", name = "variant")
public class VariantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "chromosome")
    @NotNull
    private String chrom;

    @Column(name = "position")
    @NotNull
    private long pos;

    @Column(name = "reference_allele")
    @NotNull
    private String ref;

    @ElementCollection
    @CollectionTable(name = "alternative_alleles", schema = "vcf_data", joinColumns = @JoinColumn(name = "variant_id"))
    @Column(name = "alt")
    private List<String> alts = new ArrayList<>();// Альтернативные аллели

    @Column(name = "lof_number")
    @NotNull
    private String lof;

    // One variant can have multiple annotations
    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AnnotationEntity> annotations = new ArrayList<>();

    public void addAnnotation(AnnotationEntity annotation) {
        annotations.add(annotation);
        annotation.setVariant(this);
    }

    public void removeAnnotation(AnnotationEntity annotation) {
        annotations.remove(annotation);
        annotation.setVariant(null);
    }
}

