package com.vecondev.buildoptima.api.controller;

import com.vecondev.buildoptima.api.FaqCategoryApi;
import com.vecondev.buildoptima.dto.Metadata;
import com.vecondev.buildoptima.dto.faq.request.FaqCategoryRequestDto;
import com.vecondev.buildoptima.dto.faq.response.FaqCategoryResponseDto;
import com.vecondev.buildoptima.dto.filter.FetchRequestDto;
import com.vecondev.buildoptima.dto.filter.FetchResponseDto;
import com.vecondev.buildoptima.security.user.AppUserDetails;
import com.vecondev.buildoptima.service.faq.FaqCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
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
@RequestMapping("/faq/categories")
@PreAuthorize("hasAuthority('resource_write')")
public class FaqCategoryController implements FaqCategoryApi {

  private final FaqCategoryService faqCategoryService;

  @Override
  @PostMapping
  public ResponseEntity<FaqCategoryResponseDto> create(
      @Valid @RequestBody FaqCategoryRequestDto requestDto,
      @AuthenticationPrincipal AppUserDetails user) {
    log.info(
        "Attempt to create new FAQ Category with name: {} by user with id: {}",
        requestDto.getName(),
        user.getId());

    return new ResponseEntity<>(
        faqCategoryService.create(requestDto, user.getId()), CREATED);
  }

  @Override
  @GetMapping("/{id}")
  public ResponseEntity<FaqCategoryResponseDto> getById(
      @PathVariable UUID id, @AuthenticationPrincipal AppUserDetails user) {
    log.info("Retrieving the FAQ Category with id: {} by user with id: {}", id, user.getId());

    return ResponseEntity.ok(faqCategoryService.getById(id));
  }

  @Override
  @GetMapping
  public ResponseEntity<List<FaqCategoryResponseDto>> getAll(
      @AuthenticationPrincipal AppUserDetails user) {
    log.info("Retrieving all FAQ Categories by user with id: {}", user.getId());

    return new ResponseEntity<>(faqCategoryService.getAll(), OK);
  }

  @Override
  @PostMapping("/fetch")
  public ResponseEntity<FetchResponseDto> fetch(
      @RequestBody FetchRequestDto fetchRequest, @AuthenticationPrincipal AppUserDetails user) {
    log.info("User with id: {} is fetching faq questions.", user.getId());

    return ResponseEntity.ok(faqCategoryService.fetch(fetchRequest));
  }

  @Override
  @PutMapping("/{id}")
  public ResponseEntity<FaqCategoryResponseDto> update(
      @PathVariable UUID id,
      @Valid @RequestBody FaqCategoryRequestDto requestDto,
      @AuthenticationPrincipal AppUserDetails user) {
    log.info(
        "Attempt to update the FAQ Category with id: {} by user with id: {}", id, user.getId());

    return ResponseEntity.ok(faqCategoryService.update(id, requestDto, user.getId()));
  }

  @Override
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
      @PathVariable UUID id, @AuthenticationPrincipal AppUserDetails user) {
    log.info(
        "Attempt to delete the FAQ Category with id: {} by user with id: {}", id, user.getId());

    faqCategoryService.delete(id, user.getId());
    return new ResponseEntity<>(OK);
  }

  @Override
  @GetMapping("/csv")
  public ResponseEntity<Resource> exportInCsv(@AuthenticationPrincipal AppUserDetails user) {
    log.info(
        "User with id: {} trying to export all faq categories in '.csv' format.", user.getId());

    return faqCategoryService.exportInCsv();
  }

  @Override
  @GetMapping("/metadata")
  public ResponseEntity<Metadata> getMetadata(@AuthenticationPrincipal AppUserDetails user) {
    log.info("User with id: {} trying to get the FAQ category metadata.", user.getId());

    return ResponseEntity.ok(faqCategoryService.getMetadata());
  }
}
