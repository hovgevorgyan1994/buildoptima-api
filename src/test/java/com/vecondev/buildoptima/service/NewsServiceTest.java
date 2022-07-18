package com.vecondev.buildoptima.service;

import com.vecondev.buildoptima.dto.request.news.NewsCreateRequestDto;
import com.vecondev.buildoptima.dto.request.news.NewsUpdateRequestDto;
import com.vecondev.buildoptima.dto.response.news.NewsResponseDto;
import com.vecondev.buildoptima.exception.NewsException;
import com.vecondev.buildoptima.mapper.news.NewsMapper;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

  private final NewsServiceTestParameters serviceTestParameters = new NewsServiceTestParameters();
  private final UserServiceTestParameters userServiceTestParameters =
      new UserServiceTestParameters();
  @InjectMocks private NewsServiceImpl newsService;
  @Mock private NewsRepository newsRepository;
  @Mock private ImageService imageService;
  @Mock private NewsMapper newsMapper;
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
    UUID id = UUID.randomUUID();

    when(newsRepository.existsById(id)).thenReturn(true);

    newsService.delete(id, new AppUserDetails(user));

    assertNull(newsRepository.getReferenceById(id));
  }

  @Test
  void failDeleteAsNewsEntityNotFound() {
    UUID id = UUID.randomUUID();

    when(newsRepository.existsById(id)).thenReturn(false);

    assertThrows(NewsException.class, () -> newsService.delete(id, new AppUserDetails(user)));
  }
}
