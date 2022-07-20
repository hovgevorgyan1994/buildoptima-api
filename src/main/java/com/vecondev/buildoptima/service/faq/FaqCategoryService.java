package com.vecondev.buildoptima.service.faq;

import com.vecondev.buildoptima.dto.request.faq.FaqCategoryRequestDto;
import com.vecondev.buildoptima.dto.request.filter.FetchRequestDto;
import com.vecondev.buildoptima.dto.Metadata;
import com.vecondev.buildoptima.dto.response.faq.FaqCategoryResponseDto;
import com.vecondev.buildoptima.dto.response.filter.FetchResponseDto;
import com.vecondev.buildoptima.model.faq.FaqCategory;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public interface FaqCategoryService {

  List<FaqCategoryResponseDto> getAllCategories();

  FaqCategoryResponseDto getCategoryById(UUID categoryId);

  FaqCategoryResponseDto createCategory(FaqCategoryRequestDto requestDto, UUID userId);

  FaqCategoryResponseDto updateCategory(
      UUID categoryId, FaqCategoryRequestDto requestDto, UUID userId);

  void deleteCategory(UUID categoryId, UUID userId);

  FaqCategory findCategoryById(UUID id);

  FetchResponseDto fetchCategories(FetchRequestDto fetchRequest);

  ResponseEntity<Resource> exportFaqCategoriesInCsv();

  Metadata getMetadata();
}
