package com.vecondev.buildoptima.service;

import com.vecondev.buildoptima.dto.request.filter.FetchRequestDto;
import com.vecondev.buildoptima.dto.request.news.NewsCreateRequestDto;
import com.vecondev.buildoptima.dto.request.news.NewsUpdateRequestDto;
import com.vecondev.buildoptima.dto.response.filter.FetchResponseDto;
import com.vecondev.buildoptima.dto.response.news.Metadata;
import com.vecondev.buildoptima.dto.response.news.NewsResponseDto;
import com.vecondev.buildoptima.exception.NewsException;
import com.vecondev.buildoptima.filter.converter.PageableConverter;
import com.vecondev.buildoptima.mapper.news.NewsMapper;
import com.vecondev.buildoptima.mapper.user.UserMapper;
import com.vecondev.buildoptima.model.news.News;
import com.vecondev.buildoptima.model.news.NewsCategory;
import com.vecondev.buildoptima.model.user.Role;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.parameters.news.NewsServiceTestParameters;
import com.vecondev.buildoptima.parameters.user.UserServiceTestParameters;
import com.vecondev.buildoptima.repository.news.NewsRepository;
import com.vecondev.buildoptima.repository.user.UserRepository;
import com.vecondev.buildoptima.security.user.AppUserDetails;
import com.vecondev.buildoptima.service.image.ImageService;
import com.vecondev.buildoptima.service.news.impl.NewsServiceImpl;
import com.vecondev.buildoptima.validation.validator.FieldNameValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

  private final NewsServiceTestParameters serviceTestParameters = new NewsServiceTestParameters();
  private final UserServiceTestParameters userServiceTestParameters =
      new UserServiceTestParameters();
  @InjectMocks private NewsServiceImpl newsService;
  @Mock private NewsRepository newsRepository;
  @Mock private ImageService imageService;
  @Mock private PageableConverter pageableConverter;
  @Mock private NewsMapper newsMapper;
  @Mock private UserMapper userMapper;
  @Mock private UserRepository userRepository;

  private NewsCreateRequestDto createNewsRequestDto;
  private News news;
  private User user;

  @BeforeEach()
  void init() {
    createNewsRequestDto = serviceTestParameters.getCreateNewsRequestDto();
    news = serviceTestParameters.getNewsFromCreateNewsDto(createNewsRequestDto);
    news.setId(UUID.randomUUID());
    user = userServiceTestParameters.getSavedUser();
    user.setRole(Role.ADMIN);
  }

  @Test
  void successfullyCreated() {
    when(userRepository.getReferenceById(any())).thenReturn(user);
    when(newsMapper.mapToEntity(createNewsRequestDto)).thenReturn(news);
    when(newsRepository.saveAndFlush(news)).thenReturn(news);
    when(newsMapper.mapToResponseDto(any()))
        .thenReturn(serviceTestParameters.getNewsResponseDto(news));
    NewsResponseDto responseDto =
        newsService.create(createNewsRequestDto, new AppUserDetails(user));
    assertNotNull(responseDto);
  }

  @Test
  void successfullyUpdated() {
    NewsUpdateRequestDto updateNewsRequestDto = serviceTestParameters.getUpdateNewsRequestDto();
    news.setCategory(NewsCategory.OPINION);
    news.setTitle("Winter Sales");
    NewsResponseDto responseDto = serviceTestParameters.getNewsResponseDto(news);
    doReturn(Optional.of(news)).when(newsRepository).findById(any());
    when(userRepository.getReferenceById(any())).thenReturn(user);
    when(newsMapper.mapToResponseDto(news)).thenReturn(responseDto);
    NewsResponseDto update =
        newsService.update(any(), updateNewsRequestDto, new AppUserDetails(user));
    assertEquals(news.getCategory(), update.getCategory());
    assertEquals(news.getTitle(), update.getTitle());
  }

  @Test
  void failToUpdateAsNewsEntityNotFound() {
    NewsUpdateRequestDto updateNewsRequestDto = serviceTestParameters.getUpdateNewsRequestDto();
    doThrow(NewsException.class).when(newsRepository).findById(any());
    assertThrows(
        NewsException.class,
        () -> newsService.update(any(), updateNewsRequestDto, new AppUserDetails(user)));
  }

  @Test
  void failToUpdateAsUpdateRequestDtoIsNull() {
    assertThrows(
        NewsException.class,
        () -> newsService.update(UUID.randomUUID(), null, new AppUserDetails(user)));
  }

  @Test
  void successfullyDeleted() {
    doReturn(Optional.of(news)).when(newsRepository).findById(any());
    newsService.delete(any(), new AppUserDetails(user));
    verify(newsRepository).deleteById(any());
  }

  @Test
  void failDeleteAsNewsEntityNotFound() {
    UUID id = UUID.randomUUID();
    doThrow(NewsException.class).when(newsRepository).findById(any());
    assertThrows(NewsException.class, () -> newsService.delete(id, new AppUserDetails(user)));
  }

  @Test
  void getByIdSuccess() {
    UUID id = UUID.randomUUID();
    doReturn(Optional.of(news)).when(newsRepository).findById(id);
    doReturn(serviceTestParameters.getNewsResponseDto(news))
        .when(newsMapper)
        .mapToResponseDto(news);
    NewsResponseDto responseDto = newsService.getById(id, new AppUserDetails(user));
    assertNotNull(responseDto);
  }

  @Test
  void getByIdFailedAsNewsNotFound() {
    UUID id = UUID.randomUUID();
    doThrow(NewsException.class).when(newsRepository).findById(id);
    assertThrows(NewsException.class, () -> newsService.getById(id, new AppUserDetails(user)));
  }

  @Test
  void fetchNewsSuccess() {
    FetchRequestDto requestDto = serviceTestParameters.getFetchRequest();
    Pageable pageable = userServiceTestParameters.getPageable(requestDto);
    Page<News> result = new PageImpl<>(serviceTestParameters.getFetchResponse());

    try (MockedStatic<FieldNameValidator> validator =
        Mockito.mockStatic(FieldNameValidator.class)) {
      validator
          .when(() -> FieldNameValidator.validateFieldNames(any(), any()))
          .thenAnswer((Answer<Void>) invocation -> null);
    }
    when(pageableConverter.convert(requestDto)).thenReturn(pageable);
    when(newsRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(result);
    when(newsMapper.mapToResponseList(result))
        .thenReturn(serviceTestParameters.getNewsResponseDtoList(result.stream().toList()));

    FetchResponseDto responseDto = newsService.fetch(requestDto);
    assertEquals(2, responseDto.getTotalElements());
  }

  @Test
  void getMetadataSuccess() {
    doReturn(news).when(newsRepository).findTopByOrderByUpdatedAtDesc();
    doReturn(10L).when(newsRepository).countByStatus(any());
    doReturn(userServiceTestParameters.getUserResponseDto(user))
        .when(userMapper)
        .mapToResponseDto(any());
    Metadata metadata = newsService.getMetadata(new AppUserDetails(user));
    assertNotNull(metadata);
  }
}
