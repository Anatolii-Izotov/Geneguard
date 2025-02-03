/*
 * Service class for annotating vcf file using SnpEff
 */

package com.genguard.service;

import com.genguard.entity.VariantEntity;
import com.genguard.repository.VariantRepository;
import lombok.RequiredArgsConstructor;
import org.snpeff.SnpEff;

import org.snpeff.interval.Gene;
import org.snpeff.interval.Transcript;

import org.snpeff.snpEffect.*;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class VcfProcessingService implements DefaultVcfService {

    private final VariantRepository variantRepository;

    private final DefaultVcfParserService parserService;

    private static final Logger logger = Logger.getLogger(VcfProcessingService.class.getName());

    private File annotatedFile;

    private String configPath = "/Users/anatolyizotov/IdeaProjects/geneguard/snpEff/snpEff.config";
    private Config config = new Config("GRCh37.75", configPath);

    /**
     * Annotates and filters the original VCF file.
     *
     * @param file The original VCF file to process.
     * @return
     * @throws IOException if an I/O error occurs creating or reading from files
     */
    @Override
    public void processFile(MultipartFile file) throws IOException {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("original", ".vcf");
            file.transferTo(tempFile.toFile());
            logger.log(Level.INFO, "Temporary file created at: {0}", tempFile.toAbsolutePath());

            File annotatedDir = new File("/Users/anatolyizotov/IdeaProjects/geneguard/test/annotated");
            if (!annotatedDir.exists() && !annotatedDir.mkdirs()) {
                logger.log(Level.SEVERE, "Failed to create directory: {0}", annotatedDir.getAbsolutePath());
                throw new IOException("Failed to create annotated/filtered directory.");
            }

            addVersionLine(tempFile.toFile().getAbsolutePath(), "4.2");
            File processedFile = annotate(tempFile.toFile());
            parserService.parseVcfFile(processedFile);

            annotatedFile = new File(annotatedDir, Objects.requireNonNull(file.getOriginalFilename().replace(".vcf", ".annotated.vcf")));

            Files.move(processedFile.toPath(), annotatedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            logger.log(Level.INFO, "Annotated file moved to: {0}", annotatedFile.getAbsolutePath());
//            AnnotatedVcfResponse response = annotatedFileToJson();
            //System.out.println(response.toString());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "I/O error occurred while processing the file", e);
            throw e;
        } finally {
            if (tempFile != null) {
                Files.deleteIfExists(tempFile);
                logger.log(Level.INFO, "Temporary file deleted: {0}", tempFile.toAbsolutePath());
            }
        }
    }

    /**
     * Returns the annotated and filtered VCF data as a {@link MultipartFile}.
     *
     * @return A {@link MultipartFile} containing the processed VCF data.
     * @throws IOException if an error occurs reading the processed data
     */
    public File getAnnotatedFile() throws IOException {
        return annotatedFile;
    }

    /**
     * Annotates the input VCF file using SnpEff.
     *
     * @param inputFile The input VCF file to annotate.
     * @return A {@link File} containing only annotated variants.
     * @throws IOException if an I/O error occurs during reading/writing
     */
    @Override
    public File annotate(File inputFile) throws IOException {

        SnpEff snpEff = new SnpEff();
        CustomSnpEff customSnpEff = new CustomSnpEff();
        customSnpEff.publicDataDir = "/Users/anatolyizotov/IdeaProjects/geneguard/snpEff/data";

        String configPath = "/Users/anatolyizotov/IdeaProjects/geneguard/snpEff.config";
        Config config = new Config("GRCh37.75", configPath);
        snpEff.setConfig(config);

        if (inputFile == null || !inputFile.exists()) {
            throw new IllegalArgumentException("Input file is null or does not exist: " + inputFile);
        }


        // Step 2: Prepare the command for annotation
        String inputFilePath = inputFile.getAbsolutePath();
        String annotatedFilePath = inputFilePath.replace(".vcf", ".annotated.vcf");

        File annotatedFile = new File(annotatedFilePath);

        String[] annotateArgs = {
                "GRCh37.75",                // genome version
                inputFilePath,             // your VCF
                "-o", "vcf"
        };
        // Step 3: Run the annotation command
        SnpEffCmdEff annotateCommand = new SnpEffCmdEff();

        logger.log(Level.INFO, ">>>>> parseArgs working...");
        annotateCommand.parseArgs(annotateArgs);
        logger.log(Level.INFO, ">>>>> parseArgs completed");

        try (PrintStream fileOut = new PrintStream(annotatedFile)) {
            PrintStream originalOut = System.out; // Save the original System.out
            System.setOut(fileOut);              // Redirect System.out to the file

            annotateCommand.run();               // Run SnpEff
            System.setOut(originalOut);          // Restore the original System.out
        } catch (Exception e) {
            throw new IOException("Error during annotation", e);
        }
        logger.log(Level.INFO, ">>>>> printing to file completed");
        // Step 4: Verify output and return the annotated file
        if (!annotatedFile.exists() || !annotatedFile.isFile()) {
            throw new IOException("Annotation failed. Output file not created: " + annotatedFilePath);
        }

        logger.log(Level.INFO, ">>>>> Annotated file size: ", +annotatedFile.length());
        logger.log(Level.INFO, ">>>>> Annotated file returning...");
        return annotatedFile;
    }

    public static void addVersionLine(String filePath, String version) {
        try {
            // Read all existing lines from the file
            List<String> lines = Files.readAllLines(Paths.get(filePath));

            // Create a BufferedWriter to overwrite the file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                writer.write("##fileformat=VCFv" + version);
                writer.newLine(); // Add a newline after the new content
                writer.write("#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\n");
                writer.newLine();

                // Write the old content back
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



//    public AnnotatedVcfResponse annotatedFileToJson() {
//        AnnotatedVcfResponse response = new AnnotatedVcfResponse();
//
//        VcfFileIterator vcfFileIterator = new VcfFileIterator(annotatedFile.getPath());
//        SnpEffectPredictor predictor = SnpEffectPredictor.load(config);
//        predictor.buildForest();
//        long variantCount = 0;
//        for (VcfEntry vcfEntry : vcfFileIterator) {
//
//            for (String alt : vcfEntry.getAlts()) {
//
//                // Create a SnpEff Variant object
//                Variant variant = new Variant(
//                        predictor.getGenome().getChromosome(vcfEntry.getChromosomeName()),
//                        vcfEntry.getStart(),
//                        vcfEntry.getRef(),
//                        alt
//                );
//
//                // Create our DTO
//                CustomVariant customVariant = new CustomVariant();
//                customVariant.setChromName(vcfEntry.getChromosomeName());
//                customVariant.setStart(vcfEntry.getStart());    // 1-based or 0-based? Confirm in your usage.
//                customVariant.setRef(vcfEntry.getRef());
//                customVariant.setAlt(alt);
//                // If you have a unique ID from the VCF or want to create one:
//                customVariant.setId(vcfEntry.getId());
//                // If you want to store QUAL / FILTER:
//                customVariant.setQual(vcfEntry.getQuality());
//                customVariant.setFilter(vcfEntry.getFilter()==null ? "PASS" : vcfEntry.getFilter());
//
//                // Compute the variant’s effects using SnpEff
//                VariantEffects effects = predictor.variantEffect(variant);
//
//                // For each effect (annotation)
//                for (VariantEffect effect : effects) {
//                    AnnotationDto annotationDto = new AnnotationDto();
//
//                    // 1) Basic fields
//                    annotationDto.setEffect(effect.getEffectType().toString());
//                    annotationDto.setAllele(effect.getAaAlt()); // e.g. the changed amino acid
//                    // 2) Impact
//                    if (effect.getEffectImpact() != null) {
//                        annotationDto.setImpact(effect.getEffectImpact().toString());
//
//                        // Count how many are HIGH, MODERATE, LOW, etc. for summary
//                        VariantEffect.EffectImpact impact = effect.getEffectImpact();
//                        switch (impact) {
//                            case HIGH:
//                                response.incrementHighImpact();
//                                break;
//                            case MODERATE:
//                                response.incrementModerateImpact();
//                                break;
//                            case LOW:
//                                response.incrementLowImpact();
//                                break;
//                            default:
//                                break;
//                        }
//                    }
//
//                    // 3) Gene name / gene ID (if present)
//                    annotationDto.setGeneName(effect.getGene().getGeneName());
//                    annotationDto.setGeneId(effect.getGene().getId());
//
//                    // Add this annotation to the variant
//                    customVariant.addAnnotation(annotationDto);
//                }
//                response.addVariant(customVariant);
//                variantCount++;
//
//            }
//        }
//        response.setTotalVariants(variantCount);
////        logger.log(Level.INFO, response.toString());
//        return response;
//    }
//    public JsonAlias vcfToJson(File vcfFile) {
//
//
//
//    }


//    public File annotateAndFilter(File inputFile) throws IOException {
//        File annotatedAndFilteredFile = new File(
//                "/Users/anatolyizotov/IdeaProjects/geneguard/upload/annotated_filtered/",
//                "annotated_and_filtered_" + System.currentTimeMillis() + ".vcf"
//        );
//
//        // SnpEff configuration
//        String configPath = "/Users/anatolyizotov/IdeaProjects/geneguard/snpEff/snpEff.config";
//        Config config = new Config("GRCh37.75", configPath);
//        SnpEffectPredictor predictor = SnpEffectPredictor.load(config);
//
//        // Ensure intervalForest is initialized
//        predictor.buildForest();
//        logger.log(Level.INFO, "SnpEff Predictor loaded for genome: {0}", config.getGenome());
//
//        Set<String> genesOfInterest = Set.of("HERC2", "OCA2", "MC1R", "SLC24A4", "SLC45A2", "TYR", "SRY", "XIST", "NOC2L");
//        Set<String> pathogenic = Set.of("pathogenic", "likely pathogenic");
//
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(annotatedAndFilteredFile))) {
//            VcfFileIterator vcfFileIterator = new VcfFileIterator(inputFile.getPath());
//            logger.log(Level.INFO, "VCF file iterator initialized for: {0}", inputFile.getPath());
//
//            int totalEntries = 0;
//            int filteredEntries = 0;
//
//            // Write VCF header
//            if (vcfFileIterator.getVcfHeader() != null) {
//                writer.write(vcfFileIterator.getVcfHeader().toString());
//                writer.newLine();
//            }
//
//            for (VcfEntry vcfEntry : vcfFileIterator) {
//                totalEntries++;
//                Chromosome chromosome = predictor.getGenome().getChromosome(vcfEntry.getChromosomeName());
//                if (chromosome == null) {
//                    logger.log(Level.SEVERE, "Chromosome not found: {0}", vcfEntry.getChromosomeName());
//                    continue; // Skip this entry
//                }
//
//                boolean hasRelevantEffect = false;
//                for (String alt : vcfEntry.getAlts()) {
//                    Variant variant = new Variant(
//                            predictor.getGenome().getChromosome(vcfEntry.getChromosomeName()),
//                            vcfEntry.getStart(), vcfEntry.getRef(), alt
//                    );
//                    VariantEffects effects = predictor.variantEffect(variant);
//
//                    for (VariantEffect effect : effects) {
//                        // Construct ANN field
//                        String annEntry = String.format("%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s",
//                                alt,
//                                effect.getEffectType(),
//                                effect.getEffectImpact(),
//                                effect.getGene() != null ? effect.getGene().getGeneName() : "",
//                                effect.getGene() != null ? effect.getGene().getId() : "",
//                                effect.getTranscript() != null ? "transcript" : "",
//                                effect.getTranscript() != null ? effect.getTranscript().getId() : "",
//                                effect.getTranscript() != null ? effect.getTranscript().getBioType() : "",
//                                "", // Rank (optional, requires additional logic to calculate)
//                                effect.getHgvsDna(),
//                                effect.getHgvsProt(),
//                                effect.getcDnaPos(),
//                                effect.getCodonNum(),
//                                effect.getAaChange(),
//                                effect.getDistance(),
//                                effect.getError() + "|" + effect.getWarning()
//                        );
//                        annField.append(annEntry).append(",");
//
//                        // Filtering logic
//                        String geneName = effect.getGene() != null ? effect.getGene().getGeneName() : "";
//                        if (genesOfInterest.contains(geneName)) {
//                            hasRelevantEffect = true;
//                        }
//                    }
//                }
//
//
//                if (hasRelevantEffect) {
//                    writer.write(vcfEntry.toString());
//                    writer.newLine();
//                    filteredEntries++;
//                }
//            }
//
//            writer.flush();
//            logger.log(Level.INFO, "Annotation and filtering completed. {0}/{1} entries written to {2}",
//                    new Object[]{filteredEntries, totalEntries, annotatedAndFilteredFile.getAbsolutePath()});
//
//        } catch (Exception e) {
//            logger.log(Level.SEVERE, "Exception during annotation and filtering", e);
//            throw new IOException("Error during annotation and filtering", e);
//        }
//
//        return annotatedAndFilteredFile;
//    }

        public void checkGenePresence (String geneName){
            try {
                // Load the SnpEff configuration and predictor
                String configPath = "/Users/anatolyizotov/IdeaProjects/geneguard/snpEff/snpEff.config";
                Config config = new Config("GRCh37.75", configPath);
                SnpEffectPredictor predictor = SnpEffectPredictor.load(config);

                // Ensure the genome is loaded
                if (predictor.getGenome() != null) {
                    boolean geneFound = false;

                    // Iterate through all genes in the genome
                    for (Gene gene : predictor.getGenome().getGenes()) {
                        if (gene.getGeneName().equalsIgnoreCase(geneName)) {
                            geneFound = true;

                            // Print detailed information about the gene
                            System.out.println("Gene Name: " + gene.getGeneName());
                            System.out.println("Chromosome: " + gene.getChromosomeName());
                            System.out.println("Start Position: " + gene.getStart());
                            System.out.println("End Position: " + gene.getEnd());
                            System.out.println("Strand: " + (gene.isStrandPlus() ? "+" : "-"));
                            System.out.println("Biotype: " + gene.getBioType());
                            System.out.println("Number of Transcripts: " + gene.numChilds());

                            // Iterate over each transcript and print detailed information
                            for (Transcript transcript : gene) {
                                System.out.println("  Transcript ID: " + transcript.getId());
                                System.out.println("  Biotype: " + transcript.getBioType());
                                System.out.println("  Protein Coding: " + transcript.isProteinCoding());
                                System.out.println("  CDS Start: " + transcript.getCdsStart());
                                System.out.println("  CDS End: " + transcript.getCdsEnd());
                                System.out.println("  CDS: " + transcript.cds());
                                System.out.println("  mRNA: " + transcript.mRna());
                                System.out.println("  Protein Sequence: " + transcript.protein());

                                // Check for errors or warnings in the transcript
                                if (transcript.hasErrorOrWarning()) {
                                    System.out.println("  Errors/Warnings:");
                                    if (transcript.isErrorProteinLength()) {
                                        System.out.println("    - Protein length error");
                                    }
                                    if (transcript.isErrorStartCodon()) {
                                        System.out.println("    - Missing start codon");
                                    }
                                    if (transcript.isErrorStopCodonsInCds()) {
                                        System.out.println("    - Stop codons in CDS");
                                    }
                                    if (transcript.isWarningStopCodon()) {
                                        System.out.println("    - No stop codon");
                                    }
                                }

                                System.out.println();
                            }

                            break;
                        }
                    }

                    // Output results if the gene was found
                    if (geneFound) {
                        System.out.println("Gene " + geneName + " is present in the GRCh37.75 library.");
                    } else {
                        System.out.println("Gene " + geneName + " is NOT present in the GRCh37.75 library.");
                    }
                } else {
                    System.out.println("Genome not loaded in the SnpEffectPredictor.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    @Override
    public Iterable<VariantEntity> findAllVariants() {
        return variantRepository.findAll();
    }

    @Override
    public Iterable<VariantEntity> findAllVariants(String filter) {
        if (filter != null && !filter.isBlank()) {
            return variantRepository.findAllByAnnotationsImpact(filter);
        } else {
            return variantRepository.findAll();
        }
    }

    @Override
    public void deleteVariant(Integer variantId) {
        variantRepository.deleteById(variantId);
    }
}