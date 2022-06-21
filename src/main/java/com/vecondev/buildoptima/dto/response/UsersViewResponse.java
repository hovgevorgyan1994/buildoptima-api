package com.vecondev.buildoptima.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsersViewResponse {

    private List<UserResponseDto> content;

    private int pageNo;

    private int pageSize;

    private long totalElements;

    private long totalPages;

    private boolean last;


}
