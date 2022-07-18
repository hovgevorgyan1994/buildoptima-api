package com.vecondev.buildoptima.filter.converter;

import com.vecondev.buildoptima.dto.request.filter.FetchRequestDto;
import com.vecondev.buildoptima.exception.WrongFieldException;
import com.vecondev.buildoptima.config.properties.FilterConfigProperties;
import com.vecondev.buildoptima.filter.model.SortDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

import static com.vecondev.buildoptima.exception.ErrorCode.INVALID_PAGEABLE;

@Component
@RequiredArgsConstructor
public class PageableConverter implements Converter<FetchRequestDto, Pageable> {

  private final FilterConfigProperties configProperties;

  @Nullable
  @Override
  public Pageable convert(FetchRequestDto fetchRequest) {
    Sort sort = Sort.unsorted();

    int skip = fetchRequest.getSkip() != null ? fetchRequest.getSkip() : configProperties.getSkip();
    int take = fetchRequest.getTake() != null ? fetchRequest.getTake() : configProperties.getTake();

    int page = 0;
    if (take > 0) {
      if (skip % take != 0) {
        throw new WrongFieldException(INVALID_PAGEABLE.getMessage());
      }

      page = skip / take;
    }

    if (fetchRequest.getSort() == null) {
      fetchRequest.setSort(new ArrayList<>());
    }

    for (SortDto sortDto : fetchRequest.getSort()) {
      sort =
          sort.and(
              Sort.by(Sort.Direction.fromString(sortDto.getOrder().name()), sortDto.getField()));
    }

    return new PageRequest(page, take, sort) {};
  }
}
