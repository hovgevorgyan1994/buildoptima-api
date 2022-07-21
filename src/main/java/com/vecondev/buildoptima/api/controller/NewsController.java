package com.vecondev.buildoptima.api.controller;

import com.vecondev.buildoptima.api.NewsApi;
import com.vecondev.buildoptima.dto.Metadata;
import com.vecondev.buildoptima.dto.filter.FetchRequestDto;
import com.vecondev.buildoptima.dto.filter.FetchResponseDto;
import com.vecondev.buildoptima.dto.news.request.NewsCreateRequestDto;
import com.vecondev.buildoptima.dto.news.request.NewsUpdateRequestDto;
import com.vecondev.buildoptima.dto.news.response.NewsResponseDto;
import com.vecondev.buildoptima.security.user.AppUserDetails;
import com.vecondev.buildoptima.service.news.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.Instant;
import java.util.UUID;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequiredArgsConstructor
@RequestMapping("/news")
@PreAuthorize("hasAuthority('resource_write')")
public class NewsController implements NewsApi {

  private final NewsService newsService;

  @Override
  @PostMapping(consumes = {MULTIPART_FORM_DATA_VALUE})
  public ResponseEntity<NewsResponseDto> create(
      @ModelAttribute @Valid NewsCreateRequestDto requestDto,
      @AuthenticationPrincipal AppUserDetails userDetails) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(newsService.create(requestDto, userDetails));
  }

  @Override
  @GetMapping("/{id}")
  public ResponseEntity<NewsResponseDto> getById(
      @PathVariable UUID id, @AuthenticationPrincipal AppUserDetails userDetails) {
    return ResponseEntity.ok(newsService.getById(id, userDetails));
  }

  @Override
  @PostMapping("/fetch")
  public ResponseEntity<FetchResponseDto> fetch(
      @RequestBody FetchRequestDto fetchRequestDto, @AuthenticationPrincipal AppUserDetails user) {
    return ResponseEntity.ok(newsService.fetch(fetchRequestDto, user.getUsername()));
  }

  @Override
  @PatchMapping(
      path = "/{id}",
      consumes = {MULTIPART_FORM_DATA_VALUE})
  public ResponseEntity<NewsResponseDto> update(
      @PathVariable("id") UUID id,
      @ModelAttribute @Valid NewsUpdateRequestDto requestDto,
      @AuthenticationPrincipal AppUserDetails userDetails) {
    return ResponseEntity.ok(newsService.update(id, requestDto, userDetails));
  }

  @Override
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
      @PathVariable("id") UUID id, @AuthenticationPrincipal AppUserDetails userDetails) {
    newsService.delete(id, userDetails);
    return ResponseEntity.ok().build();
  }

  @Override
  @PostMapping("/csv")
  public ResponseEntity<Resource> exportInCsv(
      @RequestBody FetchRequestDto fetchRequestDto,
      @AuthenticationPrincipal AppUserDetails userDetails) {
    String csvFileName = String.format("news-%s.csv", Instant.now());
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("application/csv"))
        .header("Content-Disposition", String.format("attachment; filename=\"%s\"", csvFileName))
        .body(newsService.exportCsv(fetchRequestDto, userDetails.getUsername()));
  }

  @Override
  @GetMapping("/metadata")
  public ResponseEntity<Metadata> getMetadata(@AuthenticationPrincipal AppUserDetails userDetails) {
    return ResponseEntity.ok(newsService.getMetadata(userDetails));
  }

  @Override
  @PatchMapping("/{id}/archive")
  public ResponseEntity<NewsResponseDto> archive(
      @PathVariable UUID id, @AuthenticationPrincipal AppUserDetails userDetails) {
    return ResponseEntity.ok(newsService.archiveNews(id, userDetails));
  }
}
