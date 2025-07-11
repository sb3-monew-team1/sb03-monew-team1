package com.sprint.mission.sb03monewteam1.controller;

import com.sprint.mission.sb03monewteam1.dto.request.InterestCreateRequestDto;
import com.sprint.mission.sb03monewteam1.dto.response.InterestResponseDto;
import com.sprint.mission.sb03monewteam1.service.InterestService;

import java.util.UUID;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InterestControllerTest {

    private final InterestService interestService = mock(InterestService.class);
    private final InterestController interestController = new InterestController(interestService);

    @Test
    void 관심사를_등록하면_DTO가_반환된다() {
        InterestCreateRequestDto request = new InterestCreateRequestDto("축구", List.of("스포츠"));
        InterestResponseDto response = new InterestResponseDto(
            UUID.randomUUID(),
            "축구",
            List.of("스포츠"),
            0L,
            false
        );
        when(interestService.create(any())).thenReturn(response);

        InterestResponseDto result = interestController.create(request);

        assertThat(result.name()).isEqualTo("축구");
    }
}
