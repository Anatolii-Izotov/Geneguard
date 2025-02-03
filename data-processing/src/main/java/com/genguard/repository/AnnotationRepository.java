package com.genguard.repository;

import com.genguard.entity.AnnotationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface AnnotationRepository extends JpaRepository<AnnotationEntity, Integer> {
    // Custom queries if needed
}

