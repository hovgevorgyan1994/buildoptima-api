package com.vecondev.buildoptima.dto.response.news;

import com.vecondev.buildoptima.dto.response.user.UserResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Metadata {

    private Instant lastUpdatedAt;
    private UserResponseDto lastUpdatedBy;
    private long allActiveCount;
    private long allArchivedCount;
}
