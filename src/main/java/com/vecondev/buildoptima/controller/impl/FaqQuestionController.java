package com.vecondev.buildoptima.controller.impl;

import com.vecondev.buildoptima.controller.FaqQuestionApi;
import com.vecondev.buildoptima.dto.request.faq.FaqQuestionRequestDto;
import com.vecondev.buildoptima.dto.request.filter.FetchRequestDto;
import com.vecondev.buildoptima.dto.response.faq.FaqQuestionResponseDto;
import com.vecondev.buildoptima.dto.response.filter.FetchResponseDto;
import com.vecondev.buildoptima.security.user.AppUserDetails;
import com.vecondev.buildoptima.service.faq.FaqQuestionService;
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
@RequestMapping("/faq/question")
public class FaqQuestionController implements FaqQuestionApi {

  private final FaqQuestionService faqQuestionService;

  @Override
  @GetMapping
  @PreAuthorize("hasAuthority('resource_read')")
  public ResponseEntity<List<FaqQuestionResponseDto>> getAllQuestions(
      @AuthenticationPrincipal AppUserDetails authenticatedUser) {
    log.info("Retrieving all FAQ Questions by user with id: {}", authenticatedUser.getId());

    return new ResponseEntity<>(faqQuestionService.getAllQuestions(), OK);
  }

  @Override
  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('resource_read')")
  public ResponseEntity<FaqQuestionResponseDto> getQuestionById(
      @PathVariable UUID id, @AuthenticationPrincipal AppUserDetails authenticatedUser) {
    log.info(
        "Retrieving the FAQ Question with id: {} by user with id: {}",
        id,
        authenticatedUser.getId());

    return ResponseEntity.ok(faqQuestionService.getQuestionById(id));
  }

  @Override
  @PostMapping
  @PreAuthorize("hasAuthority('resource_write')")
  public ResponseEntity<FaqQuestionResponseDto> createQuestion(
      @Valid @RequestBody FaqQuestionRequestDto requestDto,
      @AuthenticationPrincipal AppUserDetails authenticatedUser) {
    log.info("Attempt to create new FAQ Question by user with id: {}", authenticatedUser.getId());

    return new ResponseEntity<>(
        faqQuestionService.createQuestion(requestDto, authenticatedUser.getId()), CREATED);
  }

  @Override
  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('resource_write')")
  public ResponseEntity<FaqQuestionResponseDto> updateQuestion(
      @PathVariable UUID id,
      @Valid @RequestBody FaqQuestionRequestDto requestDto,
      @AuthenticationPrincipal AppUserDetails authenticatedUser) {
    log.info(
        "Attempt to update the FAQ Question with id: {} by user with id: {}",
        id,
        authenticatedUser.getId());

    return ResponseEntity.ok(
        faqQuestionService.updateQuestion(id, requestDto, authenticatedUser.getId()));
  }

  @Override
  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('resource_write')")
  public ResponseEntity<Void> deleteQuestion(
      @PathVariable UUID id, @AuthenticationPrincipal AppUserDetails authenticatedUser) {
    log.info(
        "Attempt to delete the FAQ Category with id: {} by user with id: {}",
        id,
        authenticatedUser.getId());

    faqQuestionService.deleteQuestion(id, authenticatedUser.getId());
    return new ResponseEntity<>(OK);
  }

  @Override
  @PostMapping("/fetch")
  @PreAuthorize("hasAuthority('resource_read')")
  public ResponseEntity<FetchResponseDto> fetchQuestions(@RequestBody FetchRequestDto fetchRequest) {

    return ResponseEntity.ok(faqQuestionService.fetchQuestions(fetchRequest));
  }
}
