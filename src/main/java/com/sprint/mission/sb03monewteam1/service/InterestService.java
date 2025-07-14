package com.sprint.mission.sb03monewteam1.service;

import com.sprint.mission.sb03monewteam1.dto.InterestDto;
import com.sprint.mission.sb03monewteam1.dto.request.InterestRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponse;

public interface InterestService {

    InterestDto create(InterestRegisterRequest request);

    CursorPageResponse getInterests(
        String searchKeyword,
        String cursor,
        int limit,
        String sortBy,
        String sortDirection);

}
