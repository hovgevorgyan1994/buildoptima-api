package com.vecondev.buildoptima.service.news.impl;

import com.vecondev.buildoptima.dto.request.news.NewsCreateRequestDto;
import com.vecondev.buildoptima.dto.request.news.NewsUpdateRequestDto;
import com.vecondev.buildoptima.dto.response.news.NewsResponseDto;
import com.vecondev.buildoptima.exception.ErrorCode;
import com.vecondev.buildoptima.exception.NewsException;
import com.vecondev.buildoptima.mapper.news.NewsMapper;
import com.vecondev.buildoptima.model.news.News;
import com.vecondev.buildoptima.model.news.NewsCategory;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.repository.news.NewsRepository;
import com.vecondev.buildoptima.repository.user.UserRepository;
import com.vecondev.buildoptima.security.user.AppUserDetails;
import com.vecondev.buildoptima.service.image.ImageService;
import com.vecondev.buildoptima.service.news.NewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

  private final NewsRepository newsRepository;
  private final UserRepository userRepository;
  private final ImageService imageService;
  private final NewsMapper newsMapper;

  @Override
  public NewsResponseDto create(
      NewsCreateRequestDto createNewsRequestDto, AppUserDetails userDetails) {
    log.info("Trying to add news item with title: {}", createNewsRequestDto.getTitle());
    User creator = userRepository.getReferenceById(userDetails.getId());
    News news = newsMapper.mapToEntity(createNewsRequestDto);
    news.setCreatedBy(creator);
    news = newsRepository.saveAndFlush(news);
    log.info("Successfully saved news item in DB");

    if (createNewsRequestDto.getImage() != null) {
      String className = news.getClass().getSimpleName().toLowerCase();
      imageService.uploadImagesToS3(
          className, news.getId(), createNewsRequestDto.getImage(), userDetails.getId());
      log.info("News image successfully uploaded to S3: news id {}", news.getId());
    }

    return newsMapper.mapToResponseDto(news);
  }

  @Override
  public NewsResponseDto update(
      UUID newsId, NewsUpdateRequestDto newsRequestDto, AppUserDetails userDetails) {
    log.info("Trying to update news item: item id {}", newsId);
    if (newsRequestDto == null) {
      log.warn(
          "In update news request 0 update fields were provided: User {}",
          userDetails.getUsername());
      throw new NewsException(ErrorCode.INVALID_NEWS_UPDATE_REQUEST);
    }
    News news =
        newsRepository
            .findById(newsId)
            .orElseThrow(() -> new NewsException(ErrorCode.NEWS_ITEM_NOT_FOUND));
    User user = userRepository.getReferenceById(userDetails.getId());

    log.info("Trying to update news entity properties");
    updateNews(newsRequestDto, news, user, userDetails);
    log.info("Successfully updated news entity properties.");

    return newsMapper.mapToResponseDto(news);
  }

  @Override
  public void delete(UUID id, AppUserDetails userDetails) {
    log.info("Trying to delete news item: news id {}", id);
    News news =
        newsRepository
            .findById(id)
            .orElseThrow(() -> new NewsException(ErrorCode.NEWS_ITEM_NOT_FOUND));
    String className = news.getClass().getSimpleName().toLowerCase();
    imageService.deleteImagesFromS3(className, id);
    newsRepository.deleteById(id);
    log.info("Successfully deleted the news item: news id was {}", id);
  }

  @Override
  public NewsResponseDto getById(UUID id, AppUserDetails userDetails) {
    log.info("User {} is trying to get news item by id: {}", userDetails.getUsername(), id);
    return newsMapper.mapToResponseDto(
        newsRepository
            .findById(id)
            .orElseThrow(() -> new NewsException(ErrorCode.NEWS_ITEM_NOT_FOUND)));
  }

  private void updateNews(
      NewsUpdateRequestDto dto, News news, User modifier, AppUserDetails userDetails) {
    if (dto.getTitle() != null) {
      news.setTitle(dto.getTitle());
    }
    if (dto.getSummary() != null) {
      news.setSummary(dto.getSummary());
    }
    if (dto.getDescription() != null) {
      news.setDescription(dto.getDescription());
    }
    if (dto.getCategory() != null) {
      news.setCategory(NewsCategory.valueOf(dto.getCategory()));
    }
    if (dto.getImage() != null) {
      String className = news.getClass().getSimpleName().toLowerCase();
      imageService.uploadImagesToS3(className, news.getId(), dto.getImage(), userDetails.getId());
    }
    news.setUpdatedBy(modifier);
  }
}
