package com.vecondev.buildoptima.service.news;

import com.vecondev.buildoptima.dto.filter.FetchRequestDto;
import com.vecondev.buildoptima.dto.news.request.NewsCreateRequestDto;
import com.vecondev.buildoptima.dto.news.request.NewsUpdateRequestDto;
import com.vecondev.buildoptima.dto.filter.FetchResponseDto;
import com.vecondev.buildoptima.dto.Metadata;
import com.vecondev.buildoptima.dto.news.response.NewsResponseDto;
import com.vecondev.buildoptima.security.user.AppUserDetails;
import org.springframework.core.io.InputStreamResource;

import java.util.UUID;

public interface NewsService {

  NewsResponseDto create(NewsCreateRequestDto dto, AppUserDetails userDetails);

  NewsResponseDto update(UUID id, NewsUpdateRequestDto dto, AppUserDetails userDetails);

  void delete(UUID id, AppUserDetails userDetails);

  NewsResponseDto getById(UUID id, AppUserDetails userDetails);

  FetchResponseDto fetch(FetchRequestDto fetchRequestDto, String username);

  Metadata getMetadata(AppUserDetails userDetails);

  InputStreamResource exportCsv(FetchRequestDto fetchRequestDto, String username);

  NewsResponseDto archiveNews(UUID id, AppUserDetails userDetails);
}
