package com.vecondev.buildoptima.util;

import com.vecondev.buildoptima.exception.FailedFileConvertionException;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.validation.UnexpectedTypeException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

import static com.vecondev.buildoptima.exception.ErrorCode.FAILED_IMAGE_CONVERSION;
import static com.vecondev.buildoptima.exception.ErrorCode.FAILED_MULTIPART_CONVERSION;
import static lombok.AccessLevel.PRIVATE;

@Slf4j
@NoArgsConstructor(access = PRIVATE)
public final class FileUtil {

  private static final Integer THUMBNAIL_WIDTH = 100;
  private static final Integer THUMBNAIL_HEIGHT = 100;

  public static File convertMultipartFileToFile(MultipartFile multipartFile) {
    File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));

    try (FileOutputStream fos = new FileOutputStream(file)) {
      fos.write(multipartFile.getBytes());
    } catch (IOException e) {
      log.error("Error occurred while converting multipart file to file.");
      throw new FailedFileConvertionException(FAILED_MULTIPART_CONVERSION);
    }

    return file;
  }

  /**
   * resizes original photo to get thumbnail version with size of 100X100
   *
   * @param originalFile the original version of photo
   * @return File the thumbnail version of original photo
   */
  public static File resizePhoto(File originalFile) {
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
      throw new FailedFileConvertionException(FAILED_IMAGE_CONVERSION);
    }
  }

  public static BufferedImage convertFileToImage(File file) {
    try {
      return ImageIO.read(file);
    } catch (IOException ex) {
      log.error("Failed to convert file to image.");
      throw new UnexpectedTypeException();
    }
  }


  /**
   * deletes file from classpath
   *
   * @throws UnexpectedTypeException when failed to delete file
   */
  public static void deleteFile(File file) {
    try {
      Files.delete(file.toPath());
      log.info("The file with name: {} is succesfully deleted from local storage.", file.getName());
    } catch (IOException ex) {
      log.error("Failed to delete file with name: {}", file.getName());
      throw new UnexpectedTypeException();
    }
  }
}
