package com.sprint.mission.sb03monewteam1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.sb03monewteam1.dto.request.InterestCreateRequestDto;
import com.sprint.mission.sb03monewteam1.dto.response.InterestResponseDto;
import com.sprint.mission.sb03monewteam1.service.InterestService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InterestController.class)
@ActiveProfiles("test")
class InterestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InterestService interestService;

    @Test
    void 관심사를_등록하면_200과_DTO가_반환된다() throws Exception {
        // Given
        InterestCreateRequestDto request = new InterestCreateRequestDto(
            "축구", List.of("스포츠")
        );

        InterestResponseDto response = new InterestResponseDto(
            UUID.randomUUID(),
            "축구",
            List.of("스포츠"),
            0L,
            false
        );

        given(interestService.create(any())).willReturn(response);

        // When & Then
        mockMvc.perform(post("/api/interests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("축구"));
    }
}
