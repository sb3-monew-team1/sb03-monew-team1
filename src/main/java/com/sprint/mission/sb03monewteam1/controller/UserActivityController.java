package com.sprint.mission.sb03monewteam1.controller;

import com.sprint.mission.sb03monewteam1.dto.UserActivityDto;
import com.sprint.mission.sb03monewteam1.service.UserActivityService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user-activities")
@RequiredArgsConstructor
public class UserActivityController {

    private final UserActivityService userActivityService;

    @GetMapping("/{userId}")
    public UserActivityDto getUserActivity(@PathVariable UUID userId) {
        return userActivityService.getUserActivity(userId);
    }
}
