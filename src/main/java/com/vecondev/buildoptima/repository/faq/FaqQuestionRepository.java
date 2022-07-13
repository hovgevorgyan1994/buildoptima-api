package com.vecondev.buildoptima.repository.faq;

import com.vecondev.buildoptima.model.faq.FaqQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FaqQuestionRepository extends JpaRepository<FaqQuestion, UUID> {
  Boolean existsByQuestionIgnoreCase(String question);
}

