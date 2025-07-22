package com.sprint.mission.sb03monewteam1.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Builder;

@Builder
public record InterestUpdateRequest(

    @NotEmpty(message = "관심사 키워드는 최소 하나 이상이어야 합니다.")
    List<String> keywords

) {

}
