package com.vecondev.buildoptima.parameters.faq.question;

import com.vecondev.buildoptima.dto.request.FetchRequestDto;
import com.vecondev.buildoptima.filter.model.Criteria;
import com.vecondev.buildoptima.filter.model.SortDto;

import java.util.List;
import java.util.Map;

import static com.vecondev.buildoptima.filter.model.SearchOperation.EQ;
import static com.vecondev.buildoptima.filter.model.SearchOperation.GT;
import static com.vecondev.buildoptima.filter.model.SearchOperation.LIKE;

public class FaqQuestionTestParameters {

    public FetchRequestDto getFetchRequest() {
        return new FetchRequestDto(
                0,
                10,
                List.of(new SortDto("answer", SortDto.Direction.ASC)),
                Map.of(
                        "and",
                        List.of(
                                new Criteria(LIKE, "question", "how"),
                                Map.of(
                                        "or",
                                        List.of(
                                                new Criteria(LIKE, "answer", "password"),
                                                new Criteria(GT, "createdAt", "2018-11-30T18:35:24.00Z"))))));
    }
}
