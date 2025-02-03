package com.genguard.service;

import com.genguard.entity.VariantEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

public interface DefaultVcfService {
    void processFile(MultipartFile file) throws InterruptedException, IOException;
    File getAnnotatedFile() throws IOException;

    File annotate(File inputFile) throws IOException;

    void checkGenePresence(String geneName);

    Iterable<VariantEntity> findAllVariants();
    Iterable<VariantEntity> findAllVariants(String filter);

    void deleteVariant(Integer variantId);
}
