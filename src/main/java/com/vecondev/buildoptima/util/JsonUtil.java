package com.vecondev.buildoptima.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vecondev.buildoptima.exception.ConvertingFailedException;
import lombok.experimental.UtilityClass;

import static com.vecondev.buildoptima.exception.Error.FAILED_JSON_CONVERTING;

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
}
