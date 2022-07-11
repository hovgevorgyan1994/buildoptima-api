package com.vecondev.buildoptima.repository.faq;

import com.vecondev.buildoptima.model.faq.FaqCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FaqCategoryRepository extends JpaRepository<FaqCategory, UUID> {}
