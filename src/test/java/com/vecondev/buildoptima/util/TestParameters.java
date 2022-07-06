package com.vecondev.buildoptima.util;

import com.vecondev.buildoptima.dto.request.FetchRequestDto;
import com.vecondev.buildoptima.filter.model.Criteria;
import com.vecondev.buildoptima.filter.model.SortDto;
import org.springframework.mock.web.MockMultipartFile;

import javax.validation.UnexpectedTypeException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static com.vecondev.buildoptima.filter.model.SearchOperation.EQ;
import static com.vecondev.buildoptima.filter.model.SearchOperation.GT;
import static com.vecondev.buildoptima.filter.model.SearchOperation.LIKE;

public class TestParameters {

  private static final String TEST_IMAGES_PATH = "src/test/resources/test-images/";

  public MockMultipartFile getMultiPartFile(String name, String contentType) {
    Path path = Paths.get(TEST_IMAGES_PATH + name);
    byte[] content;

    try {
      content = Files.readAllBytes(path);
    } catch (IOException e) {
      throw new UnexpectedTypeException();
    }

    return new MockMultipartFile("file", name, contentType, content);
  }

  public FetchRequestDto getFetchRequest() {
    return new FetchRequestDto(
        0,
        10,
        List.of(new SortDto("firstName", SortDto.Direction.ASC)),
        Map.of(
            "and",
            List.of(
                new Criteria(EQ, "firstName", "John"),
                Map.of(
                    "or",
                    List.of(
                        new Criteria(LIKE, "lastName", "Smith"),
                        new Criteria(GT, "creationDate", "2018-11-30T18:35:24.00Z"))))));
  }
}
