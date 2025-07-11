package com.sprint.mission.sb03monewteam1.service;

import com.sprint.mission.sb03monewteam1.dto.request.InterestCreateRequestDto;
import com.sprint.mission.sb03monewteam1.dto.response.InterestResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class InterestServiceImpl implements InterestService {

    @Override
    public InterestResponseDto create(InterestCreateRequestDto request) {
        return new InterestResponseDto(
            UUID.randomUUID(),
            request.name(),
            List.of("스포츠"),
            0L,
            false
        );
    }
}
