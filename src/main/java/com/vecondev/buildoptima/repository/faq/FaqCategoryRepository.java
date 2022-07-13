package com.vecondev.buildoptima.repository.faq;

import com.vecondev.buildoptima.model.faq.FaqCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FaqCategoryRepository extends JpaRepository<FaqCategory, UUID> {
    Boolean existsByNameIgnoreCase(String name);
}

