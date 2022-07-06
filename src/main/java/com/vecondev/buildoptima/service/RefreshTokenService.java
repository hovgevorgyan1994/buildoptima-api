package com.vecondev.buildoptima.service;

import com.vecondev.buildoptima.model.user.RefreshToken;

import java.util.UUID;

public interface RefreshTokenService {

    RefreshToken create(UUID userId);

    void deleteById(UUID id);

    void deleteExpiredOnes();

    RefreshToken findByUserId(UUID id);

    RefreshToken findByRefreshToken(String refreshToken);
}
