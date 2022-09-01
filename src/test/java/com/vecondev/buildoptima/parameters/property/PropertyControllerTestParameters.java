package com.vecondev.buildoptima.parameters.property;

import static com.vecondev.buildoptima.model.user.Role.CLIENT;
import static com.vecondev.buildoptima.model.user.Role.MODERATOR;

import com.vecondev.buildoptima.model.property.Details;
import com.vecondev.buildoptima.model.property.Hazards;
import com.vecondev.buildoptima.model.property.Locations;
import com.vecondev.buildoptima.model.property.Property;
import com.vecondev.buildoptima.model.property.ZoningDetails;
import com.vecondev.buildoptima.model.user.User;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class PropertyControllerTestParameters {

  private static final String TEST_FILES_PATH = "src/test/resources/files/";

  public List<User> users() {
    return List.of(
        new User("John", "Smith", "+712345678", "john@mail.ru", "John1234.", MODERATOR, true, 1),
        new User("John", "Stone", "+612345678", "john@gmail.com", "John1234/", CLIENT, true, 0));
  }

  public File getFile(String fileName) {
    Path path = Paths.get(TEST_FILES_PATH + fileName);
    if (Files.exists(path)) {
      return new File(String.valueOf(path));
    }
    return null;
  }

  public Property getProperty() {
    return Property.builder()
        .ain("123456")
        .municipality("Los Angeles")
        .addresses(List.of())
        .locations(new Locations())
        .zoningDetails(new ZoningDetails())
        .hazards(new Hazards())
        .details(new Details())
        .build();
  }
}
