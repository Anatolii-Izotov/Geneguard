package com.genguard.service;

import com.genguard.entity.AnnotationEntity;
import com.genguard.entity.VariantEntity;
import com.genguard.repository.VariantRepository;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class VcfParserService implements DefaultVcfParserService {

    private final VariantRepository variantRepository;
    private static final Logger logger = Logger.getLogger(VcfParserService.class.getName());

    @Override
    public void parseVcfFile(File annotatedFile) {
        File vcfFile = new File(annotatedFile.getAbsolutePath());

        // Open the VCF file
        try (VCFFileReader vcfReader = new VCFFileReader(vcfFile, false)) {
            // Read VCF Header
            VCFHeader header = vcfReader.getFileHeader();

            System.out.println("VCF Metadata:");
            logger.info(header.getMetaDataInInputOrder().toString());


            // Process each variant
            for (VariantContext variant : vcfReader) {

                logger.info("Processing Variant: " + variant);

                // Basic Variant Fields
                String chrom = variant.getContig();
                int pos = variant.getStart();
                String ref = variant.getReference().getBaseString();
                List<String> alts = variant.getAlternateAlleles().stream()
                        .map(Allele::getBaseString)
                        .toList();

                logger.info(String.format("CHROM: %s, POS: %d, REF: %s, ALTS: %s", chrom, pos, ref, alts));

                VariantEntity variantEntity = new VariantEntity();

                variantEntity.setId(null);
                variantEntity.setChrom(chrom);
                variantEntity.setPos(pos);
                variantEntity.setRef(ref);
                variantEntity.setAlts(alts);

                // INFO Field Parsing (e.g., ANN, LOF)
                if (variant.hasAttribute("ANN")) {
                    String annField = variant.getAttributeAsString("ANN", "");
                    String[] annotations = annField.split(",");
                    for (String annotation : annotations) {
                        String[] fields = Arrays.stream(annotation.split("\\|", -1))// -1 keeps empty fields
                                .map(str -> str.replaceAll("[\\s\\[\\]()\\.,]+", ""))
                                .toArray(String[]::new);
                        if (fields.length >= 15) {
                            System.out.printf("  ANN: Allele=%s, Impact=%s, Gene=%s, Effect=%s%n",
                                    fields[0], fields[2], fields[3], fields[1]);

                            // Save variant and annotation to database
                            AnnotationEntity annotationEntity = new AnnotationEntity();

                            annotationEntity.setId(null);
                            annotationEntity.setAlternativeAllele(fields[0]);
                            annotationEntity.setEffect(fields[1]);
                            annotationEntity.setImpact(fields[2]);
                            annotationEntity.setGeneName(fields[3]);

                            variantEntity.addAnnotation(annotationEntity);
                        } else {
                            System.out.println("  Malformed ANN entry: " + annotation);
                        }
                    }
                }

                if (variant.hasAttribute("LOF")) {
                    String lofField = variant.getAttributeAsString("LOF", "");
                    System.out.println("  LOF: " + lofField);

                    variantEntity.setLof(lofField);
                }

                // FORMAT and Sample Fields
                List<String> sampleNames = variant.getSampleNamesOrderedByName();
                for (String sample : sampleNames) {
                    String genotype = variant.getGenotype(sample).getGenotypeString();
                    System.out.printf("  Sample: %s, Genotype: %s%n", sample, genotype);

                }
                variantRepository.save(variantEntity);
            }
        } catch (Exception e) {
            logger.severe("Error reading VCF file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}