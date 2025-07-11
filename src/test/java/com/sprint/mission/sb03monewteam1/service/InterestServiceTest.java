package com.sprint.mission.sb03monewteam1.service;

import com.sprint.mission.sb03monewteam1.dto.request.InterestCreateRequestDto;
import com.sprint.mission.sb03monewteam1.dto.response.InterestResponseDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

class InterestServiceTest {

    @Test
    void 관심사를_등록하면_DTO가_반환된다() {
        InterestServiceImpl service = new InterestServiceImpl();
        InterestCreateRequestDto request = new InterestCreateRequestDto("축구", List.of("스포츠", "해외축구")
        );
        InterestResponseDto result = service.create(request);

        assertEquals("축구", result.name());
    }

}
