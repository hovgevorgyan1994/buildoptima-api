package com.vecondev.buildoptima.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Getter
@AllArgsConstructor
public enum ErrorCode {
  BAD_CREDENTIALS(UNAUTHORIZED, "Bad Credentials"),
  ACCESS_TOKEN_MISSING(UNAUTHORIZED, "Access Token Missing"),
  REFRESH_TOKEN_INVALID(BAD_REQUEST, "Invalid Refresh Token"),
  REFRESH_TOKEN_EXPIRED(FORBIDDEN, "Expired Refresh Token"),
  CREDENTIALS_NOT_FOUND(NOT_FOUND, "Credentials Not Found"),
  ACCESS_TOKEN_EXPIRED(FORBIDDEN, "Expired Access Token"),
  CONFIRM_TOKEN_NOT_FOUND(NOT_FOUND, "Confirmation Token Not Found"),
  PROVIDED_SAME_PASSWORD(CONFLICT, "Provided The Same Password In Change Password Request"),
  PROVIDED_WRONG_PASSWORD(BAD_REQUEST, "Provided Wrong Password In Change Password Request"),

  SEND_EMAIL_FAILED(INTERNAL_SERVER_ERROR, "Failed To Send An Email"),
  INVALID_PAGEABLE(BAD_REQUEST, "The `skip` Value Should Be Divisible To `take`Value"),
  INVALID_FILTER_STRUCTURE(BAD_REQUEST, "Invalid Filter Structure"),

  USER_NOT_FOUND(NOT_FOUND, "There is no user registered with such id!"),
  FAQ_QUESTION_NOT_FOUND(NOT_FOUND, "There is no FAQ question created with such id!"),
  FAQ_CATEGORY_NOT_FOUND(NOT_FOUND, "There is no FAQ category created with such id!"),
  USER_ALREADY_EXIST_WITH_EMAIL(CONFLICT, "There is an user registered with such email!"),
  USER_ALREADY_EXIST_WITH_PHONE(CONFLICT, "There is an user registered with such phone number!"),
  INVALID_IMAGE_FORMAT(PRECONDITION_FAILED, "Can't upload the image."),
  INVALID_IMAGE_SIZE(PRECONDITION_FAILED, "Image has smaller size than it's required (600x600)."),
  INVALID_FILE_SIZE(PRECONDITION_FAILED, "File's size should be between 70KB and 30MB."),
  INVALID_IMAGE_EXTENSION(
      PRECONDITION_FAILED, "The extension of the image should be either 'jpg/jpeg' or 'png'."),
  IMAGE_NOT_FOUND(NOT_FOUND, "There isn't image for the given user."),
  IMAGE_IS_REQUIRED(BAD_REQUEST, "No image was passed with request!"),
  FAILED_MULTIPART_CONVERTION(INTERNAL_SERVER_ERROR, "Error occurred while converting multipart file to file." ),
  FAILED_IMAGE_CONVERTION(INTERNAL_SERVER_ERROR, "Error occurred while resizing the image."),
  BUCKET_NOT_FOUND(INTERNAL_SERVER_ERROR, "Cant find bucket in AWS S3."),
  FAQ_CATEGORY_ALREADY_EXIST(CONFLICT, "There is a FAQ Category created with such name!"),
  FAQ_QUESTION_ALREADY_EXIST(CONFLICT, "There is a FAQ Question created with such question content!");

  private final HttpStatus httpStatus;
  private final String message;
}
