package com.vecondev.buildoptima.service.news;

import com.vecondev.buildoptima.dto.request.filter.FetchRequestDto;
import com.vecondev.buildoptima.dto.request.news.NewsCreateRequestDto;
import com.vecondev.buildoptima.dto.request.news.NewsUpdateRequestDto;
import com.vecondev.buildoptima.dto.response.filter.FetchResponseDto;
import com.vecondev.buildoptima.dto.Metadata;
import com.vecondev.buildoptima.dto.response.news.NewsResponseDto;
import com.vecondev.buildoptima.security.user.AppUserDetails;

import java.util.UUID;

public interface NewsService {

  NewsResponseDto create(NewsCreateRequestDto dto, AppUserDetails userDetails);

  NewsResponseDto update(UUID id, NewsUpdateRequestDto dto, AppUserDetails userDetails);

  void delete(UUID id, AppUserDetails userDetails);

  NewsResponseDto getById(UUID id, AppUserDetails userDetails);

  FetchResponseDto fetch(FetchRequestDto fetchRequestDto);

  Metadata getMetadata(AppUserDetails userDetails);
}
