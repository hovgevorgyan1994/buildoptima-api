package com.vecondev.buildoptima.service.news.impl;

import com.vecondev.buildoptima.dto.request.filter.FetchRequestDto;
import com.vecondev.buildoptima.dto.request.news.NewsCreateRequestDto;
import com.vecondev.buildoptima.dto.request.news.NewsUpdateRequestDto;
import com.vecondev.buildoptima.dto.response.filter.FetchResponseDto;
import com.vecondev.buildoptima.dto.response.news.Metadata;
import com.vecondev.buildoptima.dto.response.news.NewsResponseDto;
import com.vecondev.buildoptima.dto.response.user.UserResponseDto;
import com.vecondev.buildoptima.exception.ErrorCode;
import com.vecondev.buildoptima.exception.NewsException;
import com.vecondev.buildoptima.filter.converter.PageableConverter;
import com.vecondev.buildoptima.filter.model.SortDto;
import com.vecondev.buildoptima.filter.specification.GenericSpecification;
import com.vecondev.buildoptima.mapper.news.NewsMapper;
import com.vecondev.buildoptima.mapper.user.UserMapper;
import com.vecondev.buildoptima.model.Status;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.vecondev.buildoptima.filter.model.NewsFields.newsPageSortingFieldsMap;
import static com.vecondev.buildoptima.validation.validator.FieldNameValidator.validateFieldNames;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

  private final NewsRepository newsRepository;
  private final UserRepository userRepository;
  private final ImageService imageService;
  private final NewsMapper newsMapper;

  private final UserMapper userMapper;
  private final PageableConverter pageableConverter;

  @Override
  public NewsResponseDto create(
      NewsCreateRequestDto createNewsRequestDto, AppUserDetails userDetails) {
    log.info("Trying to add news item with title: {}", createNewsRequestDto.getTitle());
    User creator = userRepository.getReferenceById(userDetails.getId());
    News news = newsMapper.mapToEntity(createNewsRequestDto);
    news.setCreatedBy(creator.getId());
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

  @Override
  public FetchResponseDto fetch(FetchRequestDto fetchRequest) {
    log.info("Request to fetch news from DB");
    validateFieldNames(newsPageSortingFieldsMap, fetchRequest.getSort());
    if (fetchRequest.getSort() == null || fetchRequest.getSort().isEmpty()) {
      SortDto sortDto = new SortDto("createdAt", SortDto.Direction.DESC);
      fetchRequest.setSort(List.of(sortDto));
    }
    Pageable pageable = pageableConverter.convert(fetchRequest);
    Specification<News> specification =
        new GenericSpecification<>(newsPageSortingFieldsMap, fetchRequest.getFilter());

    assert pageable != null;
    Page<News> result = newsRepository.findAll(specification, pageable);

    List<NewsResponseDto> content = newsMapper.mapToResponseList(result);
    log.info("Response was sent. {} results where found", content.size());
    return FetchResponseDto.builder()
        .content(content)
        .page(result.getNumber())
        .size(result.getSize())
        .totalElements(result.getTotalElements())
        .last(result.isLast())
        .build();
  }

  @Override
  public Metadata getMetadata(AppUserDetails userDetails) {
    log.info("User {} is trying to get the news metadata", userDetails.getUsername());
    News lastUpdated = newsRepository.findTopByOrderByUpdatedAtDesc();
    long allActiveCount = newsRepository.countByStatus(Status.ACTIVE);
    long allArchivedCount = newsRepository.countByStatus(Status.ARCHIVED);

    UserResponseDto lastModifier =
        userMapper.mapToResponseDto(
            userRepository.getReferenceById(
                lastUpdated.getUpdatedBy() != null
                    ? lastUpdated.getUpdatedBy()
                    : lastUpdated.getCreatedBy()));

    log.info("User {} successfully got the news metadata", userDetails.getUsername());
    return Metadata.builder()
        .lastUpdatedAt(lastUpdated.getUpdatedAt())
        .lastUpdatedBy(lastModifier)
        .allActiveCount(allActiveCount)
        .allArchivedCount(allArchivedCount)
        .build();
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
    news.setUpdatedBy(modifier.getId());
    newsRepository.save(news);
  }
}
