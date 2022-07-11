package com.vecondev.buildoptima.repository.faq;

import com.vecondev.buildoptima.model.faq.FaqQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FaqQuestionRepository extends JpaRepository<FaqQuestion, UUID> {}
