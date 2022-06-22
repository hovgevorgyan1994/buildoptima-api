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
public class FetchResponse {

    private List<?> content;

    private int page;

    private int size;

    private long totalElements;

    private long totalPages;

    private boolean last;


}
