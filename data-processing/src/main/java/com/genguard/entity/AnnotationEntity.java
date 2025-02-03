package com.genguard.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "vcf_data", name = "annotation")

public class AnnotationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    @JsonIgnore
    private VariantEntity variant;

    @Column(name = "alternative_allele")
    @NotNull
    private String alternativeAllele;

    @Column(name = "effect")
    @NotNull
    private String effect;  // e.g. missense_variant

    @Column(name = "impact")
    @NotNull
    private String impact;  // e.g. MODERATE / HIGH / LOW

    @Column(name = "gene_name")
    @NotNull
    private String geneName;


//    private String geneId;
//    private String featureType;       // e.g. transcript
//    private String featureId;         // e.g. ENST00000335137
//    private String transcriptBioType; // e.g. protein_coding
//    private String rank;              // e.g. 1/1
//    private String hgvsC;             // e.g. c.759G>A
//    private String hgvsP;             // e.g. p.Trp253*
//    private String cDNAPos;           // e.g. 759/918
//    private String cDNALength;
//    private String cdsPos;            // e.g. 759/918
//    private String cdsLength;
//    private String aaPos;             // e.g. 253/305
//    private String aaLength;
//    private String distance;          // if present
//    private String errorsWarningsInfo; // collect the last ANN field
}

