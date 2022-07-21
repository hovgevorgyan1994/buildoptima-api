package com.vecondev.buildoptima.api.controller;

import com.vecondev.buildoptima.api.FaqQuestionApi;
import com.vecondev.buildoptima.dto.EntityOverview;
import com.vecondev.buildoptima.dto.Metadata;
import com.vecondev.buildoptima.dto.faq.request.FaqQuestionRequestDto;
import com.vecondev.buildoptima.dto.faq.response.FaqQuestionResponseDto;
import com.vecondev.buildoptima.dto.filter.FetchRequestDto;
import com.vecondev.buildoptima.dto.filter.FetchResponseDto;
import com.vecondev.buildoptima.filter.model.DictionaryField;
import com.vecondev.buildoptima.model.Status;
import com.vecondev.buildoptima.security.user.AppUserDetails;
import com.vecondev.buildoptima.service.faq.FaqQuestionService;
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

import static com.vecondev.buildoptima.filter.model.DictionaryField.UPDATED_BY;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/faq/questions")
@PreAuthorize("hasAuthority('resource_write')")
public class FaqQuestionController implements FaqQuestionApi {

  private final FaqQuestionService faqQuestionService;

  @Override
  @PostMapping
  public ResponseEntity<FaqQuestionResponseDto> create(
      @Valid @RequestBody FaqQuestionRequestDto requestDto,
      @AuthenticationPrincipal AppUserDetails user) {
    log.info("Attempt to create new FAQ Question by user with id: {}", user.getId());

    return new ResponseEntity<>(
        faqQuestionService.create(requestDto, user.getId()), CREATED);
  }

  @Override
  @GetMapping("/{id}")
  public ResponseEntity<FaqQuestionResponseDto> getById(
      @PathVariable UUID id, @AuthenticationPrincipal AppUserDetails user) {
    log.info("Retrieving the FAQ Question with id: {} by user with id: {}", id, user.getId());

    return ResponseEntity.ok(faqQuestionService.getById(id));
  }

  @Override
  @GetMapping
  public ResponseEntity<List<FaqQuestionResponseDto>> getAll(
      @AuthenticationPrincipal AppUserDetails user) {
    log.info("Retrieving all FAQ Questions by user with id: {}", user.getId());

    return new ResponseEntity<>(faqQuestionService.getAll(), OK);
  }

  @Override
  @PostMapping("/fetch")
  public ResponseEntity<FetchResponseDto> fetch(
      @RequestBody FetchRequestDto fetchRequest, @AuthenticationPrincipal AppUserDetails user) {
    log.info("User with id: {} is fetching faq categories.", user.getId());

    return ResponseEntity.ok(faqQuestionService.fetch(fetchRequest));
  }

  @Override
  @PutMapping("/{id}")
  public ResponseEntity<FaqQuestionResponseDto> update(
      @PathVariable UUID id,
      @Valid @RequestBody FaqQuestionRequestDto requestDto,
      @AuthenticationPrincipal AppUserDetails user) {
    log.info(
        "Attempt to update the FAQ Question with id: {} by user with id: {}", id, user.getId());

    return ResponseEntity.ok(faqQuestionService.update(id, requestDto, user.getId()));
  }

  @Override
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
      @PathVariable UUID id, @AuthenticationPrincipal AppUserDetails user) {
    log.info(
        "Attempt to delete the FAQ Category with id: {} by user with id: {}", id, user.getId());

    faqQuestionService.delete(id, user.getId());
    return new ResponseEntity<>(OK);
  }

  @Override
  @GetMapping(value = "/csv")
  public ResponseEntity<Resource> exportInCSV(@AuthenticationPrincipal AppUserDetails user) {
    log.info("User with id: {} trying to export all faq questions in '.csv' format.", user.getId());

    return faqQuestionService.exportInCsv();
  }

  @Override
  @GetMapping(value = "/metadata")
  public ResponseEntity<Metadata> getMetadata(@AuthenticationPrincipal AppUserDetails user) {
    log.info("User with id: {} trying to get the FAQ question metadata.", user.getId());

    return ResponseEntity.ok(faqQuestionService.getMetadata());
  }

  @Override
  @GetMapping("/lookup/{status}/{dictionary}")
  public ResponseEntity<List<EntityOverview>> lookup(
      @PathVariable Status status,
      @PathVariable DictionaryField dictionary,
      @AuthenticationPrincipal AppUserDetails user) {
    log.info(
        "User with id: {} looking for {} that have questions those status is: {}.",
        user.getId(),
        dictionary == UPDATED_BY ? "userS" : "categories",
        status);

    return ResponseEntity.ok(faqQuestionService.lookup(status, dictionary));
  }
}
