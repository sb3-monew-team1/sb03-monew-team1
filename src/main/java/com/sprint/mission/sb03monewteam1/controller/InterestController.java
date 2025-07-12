package com.sprint.mission.sb03monewteam1.controller;

import com.sprint.mission.sb03monewteam1.dto.request.InterestRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.response.InterestResponse;
import com.sprint.mission.sb03monewteam1.service.InterestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interests")
@RequiredArgsConstructor
public class InterestController {

    private final InterestService interestService;

    @PostMapping
    public ResponseEntity<InterestResponse> create(@Valid @RequestBody InterestRegisterRequest request) {
        InterestResponse response = interestService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
