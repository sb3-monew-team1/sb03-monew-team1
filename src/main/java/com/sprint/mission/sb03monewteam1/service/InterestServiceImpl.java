package com.sprint.mission.sb03monewteam1.service;

import com.sprint.mission.sb03monewteam1.dto.request.InterestRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.response.InterestResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class InterestServiceImpl implements InterestService {

    @Override
    public InterestResponse create(InterestRegisterRequest request) {
        return new InterestResponse(
            UUID.randomUUID(),
            request.name(),
            List.of("스포츠"),
            0L,
            false
        );
    }
}
