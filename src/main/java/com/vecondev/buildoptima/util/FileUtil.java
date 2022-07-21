package com.vecondev.buildoptima.util;

import com.vecondev.buildoptima.exception.ConvertingFailedException;
import com.vecondev.buildoptima.exception.FailedFileOperationException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

import static com.vecondev.buildoptima.exception.Error.FAILED_FILE_DELETION;
import static com.vecondev.buildoptima.exception.Error.FAILED_IMAGE_CONVERTING;
import static com.vecondev.buildoptima.exception.Error.FAILED_IMAGE_RESIZING;
import static com.vecondev.buildoptima.exception.Error.FAILED_MULTIPART_CONVERTING;

@Slf4j
@UtilityClass
public class FileUtil {

  private final Integer THUMBNAIL_WIDTH = 100;
  private final Integer THUMBNAIL_HEIGHT = 100;

  public File convertMultipartFileToFile(MultipartFile multipartFile) {
    File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));

    try (FileOutputStream fos = new FileOutputStream(file)) {
      fos.write(multipartFile.getBytes());
    } catch (IOException e) {
      log.error("Error occurred while converting multipart file to file.");
      throw new ConvertingFailedException(FAILED_MULTIPART_CONVERTING);
    }

    return file;
  }

  /**
   * resizes original photo to get thumbnail version with size of 100X100
   *
   * @param originalFile the original version of photo
   * @return File the thumbnail version of original photo
   */
  public File resizePhoto(File originalFile) {
    try {
      BufferedImage originalImage = convertFileToImage(originalFile);
      BufferedImage thumbnailImage = Scalr.resize(originalImage, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);

      String originalPath = originalFile.getAbsolutePath();
      String extension = originalPath.substring(originalPath.lastIndexOf(".") + 1);
      String thumbnailPath =
          String.format(
              "%s-%sx%s.%s",
              originalPath.substring(0, originalPath.lastIndexOf(".") - 1),
              THUMBNAIL_WIDTH,
              THUMBNAIL_HEIGHT,
              extension);

      File thumbnailFile = new File(thumbnailPath);
      ImageIO.write(thumbnailImage, extension, thumbnailFile);

      return thumbnailFile;
    } catch (IOException ex) {
      log.error("Failed to resize the original photo to get thumbnail version.");
      throw new ConvertingFailedException(FAILED_IMAGE_RESIZING);
    }
  }

  public BufferedImage convertFileToImage(File file) {
    try {
      return ImageIO.read(file);
    } catch (IOException ex) {
      log.error("Failed to convert file to image.");
      throw new ConvertingFailedException(FAILED_IMAGE_CONVERTING);
    }
  }

  /** deletes file from classpath */
  public void deleteFile(File file) {
    try {
      Files.deleteIfExists(file.toPath());
      log.info("The file with name: {} is succesfully deleted from local storage.", file.getName());
    } catch (IOException ex) {
      log.error("Failed to delete file with name: {}", file.getName());
      throw new FailedFileOperationException(FAILED_FILE_DELETION);
    }
  }
}
