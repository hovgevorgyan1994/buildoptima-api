package com.vecondev.buildoptima.service.user;

import com.vecondev.buildoptima.model.user.ConfirmationToken;
import com.vecondev.buildoptima.model.user.User;

import java.util.UUID;

public interface ConfirmationTokenService {

    ConfirmationToken create(User user);

    ConfirmationToken getByToken(String token);

    void remove(UUID id);

    void deleteExpiredOnes();

    boolean isNotValid(ConfirmationToken confirmationToken);

    void deleteByUserId(UUID id);
}