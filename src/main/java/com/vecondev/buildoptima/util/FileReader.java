package com.vecondev.buildoptima.util;

import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

@UtilityClass
public class FileReader {

  /**
   * reads a json file in given path, for open api config, to show in swagger ui the right request
   * json while searching and filtering
   *
   * @param path the given json file path
   * @return on Object of json file
   */
  public Object fetchRequestExample(String path) {
    File file = new File(path);
    try (BufferedReader reader = new BufferedReader(new java.io.FileReader(file))) {
      StringBuilder builder = new StringBuilder();
      String read;
      while ((read = reader.readLine()) != null) {
        builder.append(read);
      }
      return builder;
    } catch (IOException e) {
      throw new IllegalStateException("Could not read file");
    }
  }
}
