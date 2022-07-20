package com.vecondev.buildoptima.controller.impl;

import com.vecondev.buildoptima.controller.NewsApi;
import com.vecondev.buildoptima.dto.request.filter.FetchRequestDto;
import com.vecondev.buildoptima.dto.request.news.NewsCreateRequestDto;
import com.vecondev.buildoptima.dto.request.news.NewsUpdateRequestDto;
import com.vecondev.buildoptima.dto.response.filter.FetchResponseDto;
import com.vecondev.buildoptima.dto.Metadata;
import com.vecondev.buildoptima.dto.response.news.NewsResponseDto;
import com.vecondev.buildoptima.security.user.AppUserDetails;
import com.vecondev.buildoptima.service.news.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
  @PostMapping("/fetch")
  public ResponseEntity<FetchResponseDto> fetchNews(@RequestBody FetchRequestDto fetchRequestDto) {
    return ResponseEntity.ok(newsService.fetch(fetchRequestDto));
  }

  @Override
  @GetMapping("/{id}")
  public ResponseEntity<NewsResponseDto> getById(
      @PathVariable UUID id, @AuthenticationPrincipal AppUserDetails userDetails) {
    return ResponseEntity.ok(newsService.getById(id, userDetails));
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
  public ResponseEntity<HttpStatus> delete(
      @PathVariable("id") UUID id, @AuthenticationPrincipal AppUserDetails userDetails) {
    newsService.delete(id, userDetails);
    return ResponseEntity.ok().build();
  }

  @Override
  @GetMapping("/metadata")
  public ResponseEntity<Metadata> getMetadata(@AuthenticationPrincipal AppUserDetails userDetails) {
    return ResponseEntity.ok(newsService.getMetadata(userDetails));
  }
}
