package com.sprint.mission.sb03monewteam1.dto.request;

import java.util.List;
import lombok.Builder;

@Builder
public record InterestUpdateRequest(

    List<String> keywords

) {

}
