package com.vecondev.buildoptima.dto.news.request;

import com.vecondev.buildoptima.validation.constraint.NullOrNotBlank;
import com.vecondev.buildoptima.validation.constraint.NullOrNotEmptyList;
import com.vecondev.buildoptima.validation.constraint.ValidImage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "News Update Request DTO")
public class NewsUpdateRequestDto {

  @NullOrNotBlank private String title;
  @NullOrNotBlank private String summary;
  @NullOrNotEmptyList private List<String> keywords;
  @NullOrNotBlank private String description;
  @NullOrNotBlank private String category;
  @ValidImage private MultipartFile image;
}
