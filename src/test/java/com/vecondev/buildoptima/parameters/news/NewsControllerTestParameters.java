package com.vecondev.buildoptima.parameters.news;

import com.vecondev.buildoptima.dto.request.news.NewsCreateRequestDto;
import com.vecondev.buildoptima.dto.request.news.NewsUpdateRequestDto;
import com.vecondev.buildoptima.model.Status;
import com.vecondev.buildoptima.model.news.News;
import com.vecondev.buildoptima.model.news.NewsCategory;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.util.TestUtil;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.UUID;

import static com.vecondev.buildoptima.model.user.Role.ADMIN;
import static com.vecondev.buildoptima.model.user.Role.CLIENT;

@NoArgsConstructor
public class NewsControllerTestParameters extends TestUtil {

  private final PasswordEncoder encoder = new BCryptPasswordEncoder(12);

  public News getSavedNewsItem(NewsCreateRequestDto dto) {
    News news = News.builder()
        .title(dto.getTitle())
        .summary(dto.getSummary())
        .description(dto.getDescription())
        .status(Status.ACTIVE)
        .category(NewsCategory.valueOf(dto.getCategory()))
        .createdBy(getRightUser())
        .build();
    news.setCreatedAt(Instant.now());
    return news;
  }

  public NewsCreateRequestDto getRequestToSave() {
    return NewsCreateRequestDto.builder()
        .title("Summer Sales")
        .summary("Steam Summer Sale 2022 continues — save big on top rated PC games")
        .description("stringstringstringstringstringstringstringstringst")
        .category("BREAKING_NEWS")
        .build();
  }

  public NewsCreateRequestDto getRequestToSaveWithInvalidFields() {
    return NewsCreateRequestDto.builder()
        .title("Sum")
        .summary("Steam Summer Sale 2022 continues — save big on top rated PC games")
        .description("stringstringstrt")
        .category("UNKNOWN_CATEGORY")
        .build();
  }

  public NewsUpdateRequestDto getRequestToUpdate() {
    return NewsUpdateRequestDto.builder()
        .title("Winter Sales")
        .summary("Steam Summer Sale 2022 continues — save big on top rated PC games")
        .description("stringstringssdfjeriugbeirgurgiurgu4g4iurg4irg49rhg0urgtrt")
        .category("OPINION")
        .build();
  }

  public NewsUpdateRequestDto getRequestToUpdateWithInvalidFields() {
    return NewsUpdateRequestDto.builder()
        .title("les")
        .summary("Steam Summer Sale 2022 continues — save big on top rated PC games")
        .description("stringstgtrt")
        .category("UNKNOWN_CATEGORY")
        .build();
  }

  public User getRightUser() {
    User user = new User();
    user.setFirstName("Example");
    user.setLastName("Example");
    user.setEmail("example@gmail.com");
    user.setPassword(encoder.encode("Example234."));
    user.setRole(ADMIN);
    user.setCreatedAt(Instant.now());
    user.setEnabled(true);
    user.setId(UUID.randomUUID());
    return user;
  }

  public User getWrongUser() {
    User user = new User();
    user.setFirstName("Example");
    user.setLastName("Example");
    user.setEmail("example@gmail.com");
    user.setPassword(encoder.encode("Example234."));
    user.setRole(CLIENT);
    user.setCreatedAt(Instant.now());
    user.setEnabled(true);
    user.setId(UUID.randomUUID());
    return user;
  }
}