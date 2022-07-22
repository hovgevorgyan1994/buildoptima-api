package com.vecondev.buildoptima.service;

import com.vecondev.buildoptima.csv.news.NewsRecord;
import com.vecondev.buildoptima.dto.Metadata;
import com.vecondev.buildoptima.dto.filter.FetchRequestDto;
import com.vecondev.buildoptima.dto.filter.FetchResponseDto;
import com.vecondev.buildoptima.dto.news.request.NewsCreateRequestDto;
import com.vecondev.buildoptima.dto.news.request.NewsUpdateRequestDto;
import com.vecondev.buildoptima.dto.news.response.NewsResponseDto;
import com.vecondev.buildoptima.exception.ConvertingFailedException;
import com.vecondev.buildoptima.exception.NewsException;
import com.vecondev.buildoptima.filter.converter.PageableConverter;
import com.vecondev.buildoptima.mapper.news.NewsMapper;
import com.vecondev.buildoptima.mapper.user.UserMapper;
import com.vecondev.buildoptima.model.Status;
import com.vecondev.buildoptima.model.news.News;
import com.vecondev.buildoptima.model.news.NewsCategory;
import com.vecondev.buildoptima.model.user.Role;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.parameters.news.NewsServiceTestParameters;
import com.vecondev.buildoptima.parameters.user.UserServiceTestParameters;
import com.vecondev.buildoptima.repository.news.NewsRepository;
import com.vecondev.buildoptima.repository.user.UserRepository;
import com.vecondev.buildoptima.security.user.AppUserDetails;
import com.vecondev.buildoptima.service.auth.SecurityContextService;
import com.vecondev.buildoptima.service.csv.CsvService;
import com.vecondev.buildoptima.service.image.ImageService;
import com.vecondev.buildoptima.service.news.NewsServiceImpl;
import com.vecondev.buildoptima.validation.validator.FieldNameValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

  private final NewsServiceTestParameters serviceTestParameters = new NewsServiceTestParameters();
  private final UserServiceTestParameters userServiceTestParameters =
      new UserServiceTestParameters();
  @InjectMocks private NewsServiceImpl newsService;
  @Mock private NewsRepository newsRepository;
  @Mock private ImageService imageService;
  @Mock private SecurityContextService securityContextService;
  @Mock private PageableConverter pageableConverter;
  @Mock private NewsMapper newsMapper;
  @Mock private UserMapper userMapper;
  @Mock private UserRepository userRepository;
  @Mock private CsvService<NewsRecord> csvService;

  private NewsCreateRequestDto createNewsRequestDto;
  private News news;
  private User user;

  @BeforeEach()
  void init() {
    createNewsRequestDto = serviceTestParameters.getCreateNewsRequestDto();
    user = userServiceTestParameters.getSavedUser();
    user.setRole(Role.ADMIN);
    News newsToSave = serviceTestParameters.getNewsFromCreateNewsDto(createNewsRequestDto);
    newsToSave.setCreatedBy(user.getId());
    newsToSave.setUpdatedBy(user.getId());
    news = serviceTestParameters.getSavedNews(createNewsRequestDto, user);

  }

  @AfterEach
  void tearDown() {
    newsRepository.deleteAll();
  }

  @Test
  void successfullyCreated() {
    when(securityContextService.getUserDetails()).thenReturn(userServiceTestParameters.userDetails());
    when(userRepository.getReferenceById(any())).thenReturn(user);
    when(newsMapper.mapToEntity(createNewsRequestDto)).thenReturn(news);
    when(newsRepository.saveAndFlush(news)).thenReturn(news);
    when(newsMapper.mapToResponseDto(any()))
        .thenReturn(serviceTestParameters.getNewsResponseDto(news));

    NewsResponseDto responseDto = newsService.create(createNewsRequestDto);
    assertNotNull(responseDto);
  }

  @Test
  void successfullyUpdated() {
    NewsUpdateRequestDto updateNewsRequestDto = serviceTestParameters.getUpdateNewsRequestDto();
    news.setCategory(NewsCategory.OPINION);
    news.setTitle("Winter Sales");
    NewsResponseDto responseDto = serviceTestParameters.getNewsResponseDto(news);

    when(securityContextService.getUserDetails()).thenReturn(userServiceTestParameters.userDetails());
    doReturn(Optional.of(news)).when(newsRepository).findById(any());
    when(userRepository.getReferenceById(any())).thenReturn(user);
    when(newsMapper.mapToResponseDto(news)).thenReturn(responseDto);

    NewsResponseDto update = newsService.update(UUID.randomUUID(), updateNewsRequestDto);
    assertEquals(news.getCategory(), update.getCategory());
    assertEquals(news.getTitle(), update.getTitle());
  }

  @Test
  void failToUpdateAsNewsEntityNotFound() {
    NewsUpdateRequestDto updateNewsRequestDto = serviceTestParameters.getUpdateNewsRequestDto();

    when(securityContextService.getUserDetails()).thenReturn(userServiceTestParameters.userDetails());
    doThrow(NewsException.class).when(newsRepository).findById(any());

    assertThrows(NewsException.class, () -> newsService.update(UUID.randomUUID(), updateNewsRequestDto));
  }

  @Test
  void successfullyDeleted() {
    UUID id = UUID.randomUUID();

    doReturn(Optional.of(news)).when(newsRepository).findById(id);

    newsService.delete(id);
    verify(newsRepository).deleteById(id);
  }

  @Test
  void failDeleteAsNewsEntityNotFound() {
    UUID id = UUID.randomUUID();

    doThrow(NewsException.class).when(newsRepository).findById(id);

    assertThrows(NewsException.class, () -> newsService.delete(id));
  }

  @Test
  void getByIdSuccess() {
    UUID id = UUID.randomUUID();

    when(securityContextService.getUserDetails()).thenReturn(userServiceTestParameters.userDetails());
    doReturn(Optional.of(news)).when(newsRepository).findById(id);
    doReturn(serviceTestParameters.getNewsResponseDto(news))
        .when(newsMapper)
        .mapToResponseDto(news);

    NewsResponseDto responseDto = newsService.getById(id);
    assertNotNull(responseDto);
  }

  @Test
  void getByIdFailedAsNewsNotFound() {
    UUID id = UUID.randomUUID();

    when(securityContextService.getUserDetails()).thenReturn(userServiceTestParameters.userDetails());
    doThrow(NewsException.class).when(newsRepository).findById(id);

    assertThrows(NewsException.class, () -> newsService.getById(id));
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

    when(securityContextService.getUserDetails()).thenReturn(userServiceTestParameters.userDetails());
    when(pageableConverter.convert(requestDto)).thenReturn(pageable);
    when(newsRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(result);
    when(newsMapper.mapToResponseList(result))
        .thenReturn(serviceTestParameters.getNewsResponseDtoList(result.stream().toList()));

    FetchResponseDto responseDto = newsService.fetch(requestDto);
    assertEquals(2, responseDto.getTotalElements());
  }

  @Test
  void getMetadataSuccess() {
    long allActiveCount = 10L;

    when(securityContextService.getUserDetails()).thenReturn(userServiceTestParameters.userDetails());
    doReturn(news).when(newsRepository).findTopByOrderByUpdatedAtDesc();
    doReturn(allActiveCount).when(newsRepository).countByStatus(Status.ACTIVE);
    doReturn(userServiceTestParameters.getUserOverView(user)).when(userMapper).mapToOverview(any());

    Metadata metadata = newsService.getMetadata();
    assertNotNull(metadata);
    assertEquals(allActiveCount, metadata.getAllActiveCount());
  }

  @Test
  void exportCsvSuccess() {
    FetchRequestDto fetchRequest = serviceTestParameters.getFetchRequest();
    List<News> fetchResponse = serviceTestParameters.getFetchResponse();
    List<NewsRecord> newsRecordList = serviceTestParameters.getNewsRecordList(fetchResponse);

    when(securityContextService.getUserDetails()).thenReturn(userServiceTestParameters.userDetails());
    when(newsRepository.findAll(any(Specification.class))).thenReturn(fetchResponse);
    when(newsMapper.mapToNewsRecordList(any())).thenReturn(newsRecordList);
    when(csvService.writeToCsv(newsRecordList, NewsRecord.class))
        .thenReturn(new ByteArrayInputStream(new byte[] {}));

    Resource resource = newsService.exportCsv(fetchRequest);
    assertNotNull(resource);
    verify(newsRepository).findAll(any(Specification.class));
  }

  @Test
  void failedExportCsv() {
    FetchRequestDto fetchRequest = serviceTestParameters.getFetchRequest();
    List<News> fetchResponse = serviceTestParameters.getFetchResponse();
    List<NewsRecord> newsRecordList = serviceTestParameters.getNewsRecordList(fetchResponse);

    when(securityContextService.getUserDetails()).thenReturn(userServiceTestParameters.userDetails());
    when(newsRepository.findAll(any(Specification.class))).thenReturn(fetchResponse);
    when(newsMapper.mapToNewsRecordList(any())).thenReturn(newsRecordList);
    doThrow(ConvertingFailedException.class)
        .when(csvService)
        .writeToCsv(newsRecordList, NewsRecord.class);

    assertThrows(ConvertingFailedException.class, () -> newsService.exportCsv(fetchRequest));
    verify(newsRepository).findAll(any(Specification.class));
  }

  @Test
  void archiveNewsSuccess() {
    UUID newsId = news.getId();
    NewsResponseDto newsResponseDto = serviceTestParameters.getNewsResponseDto(news);
    newsResponseDto.setStatus(Status.ARCHIVED);

    when(securityContextService.getUserDetails()).thenReturn(userServiceTestParameters.userDetails());
    doReturn(Optional.of(news)).when(newsRepository).findById(newsId);
    doReturn(newsResponseDto).when(newsMapper).mapToResponseDto(news);

    NewsResponseDto responseDto = newsService.archiveNews(newsId);

    assertNotNull(responseDto);
    assertEquals(Status.ARCHIVED, responseDto.getStatus());
  }

  @Test
  void failedArchiveNewsAsNotFound() {
    UUID newsId = UUID.randomUUID();

    when(securityContextService.getUserDetails()).thenReturn(userServiceTestParameters.userDetails());
    doThrow(NewsException.class).when(newsRepository).findById(newsId);

    assertThrows(NewsException.class, () -> newsService.archiveNews(newsId));

    verify(newsRepository).findById(newsId);
  }
}
