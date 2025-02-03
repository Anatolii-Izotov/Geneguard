-- Create the schema
CREATE SCHEMA IF NOT EXISTS vcf_data;

-- Create the main variant table
CREATE TABLE vcf_data.variant
(
    id               SERIAL PRIMARY KEY,
    chromosome       VARCHAR(255),
    position         BIGINT,
    reference_allele VARCHAR(255),
    lof_number       VARCHAR(255)
);

-- Create the table for alternative alleles (the element collection)
CREATE TABLE vcf_data.alternative_alleles
(
    variant_id INTEGER,
    alt        VARCHAR(255),
    PRIMARY KEY (variant_id, alt),
    CONSTRAINT fk_variant
        FOREIGN KEY (variant_id)
            REFERENCES vcf_data.variant (id)
            ON DELETE CASCADE
);

-- Create the table for annotations (one-to-many relationship)
-- Adjust the columns based on your AnnotationEntity definition.
CREATE TABLE vcf_data.annotation
(
    id                 SERIAL PRIMARY KEY,
    variant_id         INTEGER,
    alternative_allele VARCHAR(255),
    effect             VARCHAR(255),
    impact             VARCHAR(255),
    gene_name          VARCHAR(255),
    CONSTRAINT fk_variant_annotation
        FOREIGN KEY (variant_id)
            REFERENCES vcf_data.variant (id)
            ON DELETE CASCADE
);
