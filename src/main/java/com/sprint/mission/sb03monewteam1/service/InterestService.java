package com.sprint.mission.sb03monewteam1.service;

import com.sprint.mission.sb03monewteam1.dto.InterestDto;
import com.sprint.mission.sb03monewteam1.dto.SubscriptionDto;
import com.sprint.mission.sb03monewteam1.dto.request.InterestRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.request.InterestUpdateRequest;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponse;
import java.util.UUID;

public interface InterestService {

    InterestDto create(InterestRegisterRequest request);

    CursorPageResponse<InterestDto> getInterests(
        UUID userId,
        String searchKeyword,
        String cursor,
        int limit,
        String sortBy,
        String sortDirection);

    SubscriptionDto createSubscription(UUID interestId, UUID userId);

    InterestDto updateInterestKeywords(UUID interestId, InterestUpdateRequest request, UUID userId);

    void deleteInterest(UUID interestId);

    void deleteSubscription(UUID userId, UUID interestId);
}
