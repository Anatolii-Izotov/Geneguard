package com.genguard.controller;

import com.genguard.entity.VariantEntity;
import com.genguard.service.DefaultVcfService;
import lombok.RequiredArgsConstructor;
import org.snpeff.interval.Variant;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.logging.Logger;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/data-processing")
public class VcfController {
    private final DefaultVcfService vcfService;
    private static final Logger logger = Logger.getLogger(VcfController.class.getName());

    @PostMapping(path = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> uploadGeneticFile(@RequestParam("file") MultipartFile file) throws IOException {

        try {
            vcfService.processFile(file);
        } catch (Exception e) {
            System.out.println("ERROR" + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok("File uploaded successfully.");
    }

    @GetMapping("/homepage")
    public Iterable<VariantEntity> getHomePage() {
        return vcfService.findAllVariants();
    }

    @GetMapping("/homepage/variants")
    public Iterable<VariantEntity> findVariants(@RequestParam(value = "filter", required = false) String filter) {
        return vcfService.findAllVariants(filter);
    }

    @DeleteMapping("/variants/{variantId}")
    public ResponseEntity<?> deleteVariant(@PathVariable Integer variantId) {
        vcfService.deleteVariant(variantId);
        return ResponseEntity.ok("Variant deleted successfully.");
    }
}
