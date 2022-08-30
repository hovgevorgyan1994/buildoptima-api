package com.vecondev.buildoptima.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vecondev.buildoptima.dto.property.PropertyListDto;
import com.vecondev.buildoptima.exception.ConvertingFailedException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

import static com.vecondev.buildoptima.exception.Error.FAILED_JSON_CONVERTING;

@Slf4j
@UtilityClass
public class JsonUtil {

  public String writeToJson(Object object) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.registerModule(new JavaTimeModule());
      objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new ConvertingFailedException(FAILED_JSON_CONVERTING);
    }
  }

  public PropertyListDto readFromJson(File file) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    PropertyListDto propertyListDto = mapper.readValue(file, new TypeReference<>() {});
    log.info("Trying to remove processed file from local storage");
    FileUtil.deleteFile(file);
    return propertyListDto;
  }
}
