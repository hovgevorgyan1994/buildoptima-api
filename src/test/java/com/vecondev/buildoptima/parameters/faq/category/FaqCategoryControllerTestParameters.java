package com.vecondev.buildoptima.parameters.faq.category;

import com.vecondev.buildoptima.dto.request.faq.FaqCategoryRequestDto;
import com.vecondev.buildoptima.exception.FaqCategoryNotFoundException;
import com.vecondev.buildoptima.model.faq.FaqCategory;
import com.vecondev.buildoptima.model.user.User;
import com.vecondev.buildoptima.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;

import static com.vecondev.buildoptima.exception.ErrorCode.FAQ_CATEGORY_NOT_FOUND;
import static com.vecondev.buildoptima.model.user.Role.CLIENT;
import static com.vecondev.buildoptima.model.user.Role.MODERATOR;

@RequiredArgsConstructor
public class FaqCategoryControllerTestParameters {

    private final UserRepository userRepository;

    public List<User> users() {
        return List.of(
                new User(
                        "John",
                        "Smith",
                        "+712345678",
                        "john@mail.ru",
                        "John1234.",
                        MODERATOR,
                        true),
                new User(
                        "John",
                        "Stone",
                        "+612345678",
                        "john@gmail.com",
                        "John1234/",
                        CLIENT,
                        true));
    }

    public List<FaqCategory> faqCategories() {
    return List.of(
        new FaqCategory(
            "Royalties",
            userRepository.findAll().stream()
                .filter(user -> user.getRole() == MODERATOR)
                .findFirst()
                .orElse(null),
            userRepository.findAll().stream()
                .filter(user -> user.getRole() == MODERATOR)
                .findFirst()
                .orElse(null),
            Instant.now(),
            Instant.now()),
        new FaqCategory(
            "Registration",
            userRepository.findAll().stream()
                .filter(user -> user.getRole() == MODERATOR)
                .findFirst()
                .orElse(null),
            userRepository.findAll().stream()
                .filter(user -> user.getRole() == MODERATOR)
                .findFirst()
                .orElse(null),
            Instant.now(),
            Instant.now()));
    }

    public FaqCategoryRequestDto getFaqCategoryToSave() {
        return new FaqCategoryRequestDto("Password recovery");
    }

    public FaqCategoryRequestDto getFaqCategoryWithDuplicatedNameToSave() {
    return new FaqCategoryRequestDto(
        faqCategories().stream()
            .findAny()
            .orElseThrow(() -> new FaqCategoryNotFoundException(FAQ_CATEGORY_NOT_FOUND))
            .getName());
    }
}