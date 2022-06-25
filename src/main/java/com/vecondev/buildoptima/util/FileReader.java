package com.vecondev.buildoptima.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

public class FileReader {

  private FileReader() {}

  public static Object fetchRequestExample(String path) {
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
