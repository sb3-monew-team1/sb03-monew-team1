package com.sprint.mission.sb03monewteam1.service;

import com.sprint.mission.sb03monewteam1.dto.InterestDto;
import com.sprint.mission.sb03monewteam1.dto.request.InterestCreateRequestDto;

public interface InterestService {
    InterestDto create(InterestCreateRequestDto request);
}
