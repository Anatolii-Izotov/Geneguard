package com.genguard.repository;

import com.genguard.entity.VariantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VariantRepository extends JpaRepository<VariantEntity, Integer> {
    Iterable<VariantEntity> findAllByAnnotationsImpact(String impact);
    // Custom queries if needed
}