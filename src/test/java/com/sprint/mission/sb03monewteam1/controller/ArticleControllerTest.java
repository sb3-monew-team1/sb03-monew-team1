package com.sprint.mission.sb03monewteam1.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.mission.sb03monewteam1.dto.ArticleDto;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponseArticleDto;
import com.sprint.mission.sb03monewteam1.service.ArticleService;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ArticleController.class)
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArticleService articleService;

    @Test
    void 기사_목록_조회_성공() throws Exception {
        // given
        List<ArticleDto> articles = Arrays.asList(
            ArticleDto.builder()
                .id(UUID.randomUUID())
                .title("테스트 기사")
                .summary("테스트 요약")
                .source("연합뉴스")
                .build());

        CursorPageResponseArticleDto response = CursorPageResponseArticleDto.builder()
            .articles(articles)
            .hasNext(false)
            .nextCursor(null)
            .build();

        when(articleService.getArticles(any(), any(), any(), any(), any(), any(), any(), anyInt()))
            .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/articles")
                .param("limit", "10")
                .param("sortBy", "publishDate")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.articles").isArray())
            .andExpect(jsonPath("$.articles", hasSize(1)))
            .andExpect(jsonPath("$.hasNext").value(false))
            .andExpect(jsonPath("$.nextCursor").isEmpty());
    }

    @Test
    void 기사_목록_조회_검색_키워드() throws Exception {
        // 검색 키워드 테스트
    }

    @Test
    void 기사_목록_조회_페이지네이션() throws Exception {
        // 페이지네이션 테스트
    }

    @Test
    void 기사_출처_목록_조회_성공() throws Exception {
        // given
        List<String> sources = Arrays.asList("연합뉴스", "중앙일보", "한국일보");
        when(articleService.getSources()).thenReturn(sources);

        // when & then
        mockMvc.perform(get("/api/articles/sources")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0]").value("연합뉴스"));
    }
}
