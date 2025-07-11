package com.sprint.mission.sb03monewteam1.controller;

import com.sprint.mission.sb03monewteam1.dto.request.InterestCreateRequestDto;
import com.sprint.mission.sb03monewteam1.dto.response.InterestResponseDto;
import com.sprint.mission.sb03monewteam1.service.InterestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interests")
@RequiredArgsConstructor
public class InterestController {

    private final InterestService interestService;

    @PostMapping
    public InterestResponseDto create(@RequestBody InterestCreateRequestDto request) {
        return interestService.create(request);
    }
}
