package com.sprint.mission.sb03monewteam1.controller;

import com.sprint.mission.sb03monewteam1.service.InterestService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interests")
@RequiredArgsConstructor
public class InterestController {

    private final InterestService interestService;

}
