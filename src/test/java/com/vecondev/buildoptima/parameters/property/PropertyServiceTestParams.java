package com.vecondev.buildoptima.parameters.property;

import com.amazonaws.services.s3.model.S3Object;
import com.vecondev.buildoptima.dto.property.PropertyListDto;
import com.vecondev.buildoptima.dto.property.PropertyReadDto;
import com.vecondev.buildoptima.util.TestUtil;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PropertyServiceTestParams extends TestUtil {

  public List<S3Object> getObjectsFromBucket() {
    return new ArrayList<>(List.of(new S3Object(), new S3Object(), new S3Object()));
  }

  public Path convertS3ObjectToPath(){
    return Paths.get("anyPath");
  }

  public PropertyListDto readFromJson() {
    List<PropertyReadDto> propertyReadDtoList =
        List.of(new PropertyReadDto(), new PropertyReadDto(), new PropertyReadDto());
    return new PropertyListDto(propertyReadDtoList);
  }
}
