package com.vecondev.buildoptima.controller.impl;

import com.vecondev.buildoptima.controller.FaqCategoryApi;
import com.vecondev.buildoptima.dto.request.FetchRequestDto;
import com.vecondev.buildoptima.dto.request.faq.FaqCategoryRequestDto;
import com.vecondev.buildoptima.dto.response.FetchResponseDto;
import com.vecondev.buildoptima.dto.response.faq.FaqCategoryResponseDto;
import com.vecondev.buildoptima.security.user.AppUserDetails;
import com.vecondev.buildoptima.service.faq.FaqCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/faq/category")
public class FaqCategoryController implements FaqCategoryApi {

  private final FaqCategoryService faqCategoryService;

  @Override
  @GetMapping
  @PreAuthorize("hasAuthority('resource_read')")
  public ResponseEntity<List<FaqCategoryResponseDto>> getAllCategories(
      @AuthenticationPrincipal AppUserDetails authenticatedUser) {
    log.info("Retrieving all FAQ Categories by user with id: {}", authenticatedUser.getId());

    return new ResponseEntity<>(faqCategoryService.getAllCategories(), OK);
  }

  @Override
  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('resource_read')")
  public ResponseEntity<FaqCategoryResponseDto> getCategoryById(
      @PathVariable UUID id, @AuthenticationPrincipal AppUserDetails authenticatedUser) {
    log.info(
        "Retrieving the FAQ Category with id: {} by user with id: {}",
        id,
        authenticatedUser.getId());

    return ResponseEntity.ok(faqCategoryService.getCategoryById(id));
  }

  @Override
  @PostMapping
  @PreAuthorize("hasAuthority('resource_write')")
  public ResponseEntity<FaqCategoryResponseDto> createCategory(
      @Valid @RequestBody FaqCategoryRequestDto requestDto,
      @AuthenticationPrincipal AppUserDetails authenticatedUser) {
    log.info(
        "Attempt to create new FAQ Category with name: {} by user with id: {}",
        requestDto.getName(),
        authenticatedUser.getId());

    return new ResponseEntity<>(
        faqCategoryService.createCategory(requestDto, authenticatedUser.getId()), CREATED);
  }

  @Override
  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('resource_write')")
  public ResponseEntity<FaqCategoryResponseDto> updateCategory(
      @PathVariable UUID id,
      @Valid @RequestBody FaqCategoryRequestDto requestDto,
      @AuthenticationPrincipal AppUserDetails authenticatedUser) {
    log.info(
        "Attempt to update the FAQ Category with id: {} by user with id: {}",
        id,
        authenticatedUser.getId());

    return ResponseEntity.ok(
        faqCategoryService.updateCategory(id, requestDto, authenticatedUser.getId()));
  }

  @Override
  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('resource_write')")
  public ResponseEntity<Void> deleteCategory(
      @PathVariable UUID id, @AuthenticationPrincipal AppUserDetails authenticatedUser) {
    log.info(
        "Attempt to delete the FAQ Category with id: {} by user with id: {}",
        id,
        authenticatedUser.getId());

    faqCategoryService.deleteCategory(id, authenticatedUser.getId());
    return new ResponseEntity<>(OK);
  }

  @Override
  @PostMapping("/fetch")
  @PreAuthorize("hasAuthority('resource_read')")
  public ResponseEntity<FetchResponseDto> fetchCategories(@RequestBody FetchRequestDto fetchRequest) {

    return ResponseEntity.ok(faqCategoryService.fetchCategories(fetchRequest));
  }
}
