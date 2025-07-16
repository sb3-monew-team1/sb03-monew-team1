package com.sprint.mission.sb03monewteam1.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.mission.sb03monewteam1.dto.ArticleDto;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponse;
import com.sprint.mission.sb03monewteam1.service.ArticleService;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ArticleController.class)
@ActiveProfiles("test")
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArticleService articleService;

    @Test
    void 기사_목록_조회_성공_기본값() throws Exception {
        // given
        List<ArticleDto> articles = Arrays.asList(
            ArticleDto.builder()
                .id(UUID.randomUUID())
                .title("테스트 기사")
                .summary("테스트 요약")
                .source("연합뉴스")
                .build());

        CursorPageResponse<ArticleDto> response = CursorPageResponse.<ArticleDto>builder()
            .content(articles)
            .nextCursor(null)
            .nextAfter(null)
            .size(1)
            .totalElements(null)
            .hasNext(false)
            .build();

        when(articleService.getArticles(
            isNull(), isNull(), isNull(), isNull(), isNull(),
            eq("publishDate"), isNull(), isNull(), isNull(), eq(10)))
            .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/articles")
                .param("limit", "10")
                .param("orderBy", "publishDate")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].title").value("테스트 기사"))
            .andExpect(jsonPath("$.hasNext").value(false))
            .andExpect(jsonPath("$.nextCursor").isEmpty())
            .andExpect(jsonPath("$.size").value(1))
            .andExpect(jsonPath("$.totalElements").isEmpty());
    }

    @Test
    void 기사_목록_조회_성공_검색_키워드_적용() throws Exception {
        // given
        String keyword = "코로나";
        List<String> sourceIn = Arrays.asList("네이버뉴스", "조선일보");

        List<ArticleDto> articles = Arrays.asList(
            ArticleDto.builder()
                .id(UUID.randomUUID())
                .title("코로나 확진자 급증")
                .summary("코로나19 확진자가 급증하고 있습니다")
                .source("네이버뉴스")
                .build(),
            ArticleDto.builder()
                .id(UUID.randomUUID())
                .title("코로나 백신 접종률")
                .summary("전국 코로나 백신 접종률이 80%를 넘었습니다")
                .source("조선일보")
                .build());

        CursorPageResponse<ArticleDto> response = CursorPageResponse.<ArticleDto>builder()
            .content(articles)
            .nextCursor(null)
            .nextAfter(null)
            .size(2)
            .totalElements(null)
            .hasNext(false)
            .build();

        when(articleService.getArticles(
            eq(keyword), eq(sourceIn), isNull(), isNull(), isNull(),
            eq("publishDate"), eq("DESC"), isNull(), isNull(), eq(10)))
            .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/articles")
                .param("keyword", keyword)
                .param("sourceIn", "네이버뉴스", "조선일보")
                .param("orderBy", "publishDate")
                .param("direction", "DESC")
                .param("limit", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content", hasSize(2)))
            .andExpect(jsonPath("$.content[0].title").value("코로나 확진자 급증"))
            .andExpect(jsonPath("$.content[1].title").value("코로나 백신 접종률"))
            .andExpect(jsonPath("$.hasNext").value(false))
            .andExpect(jsonPath("$.size").value(2));
    }

    @Test
    void 기사_목록_조회_성공_조회수_정렬_오름차순() throws Exception {
        // given
        List<ArticleDto> articles = Arrays.asList(
            ArticleDto.builder()
                .id(UUID.randomUUID())
                .title("조회수 낮은 기사")
                .viewCount(10L)
                .build(),
            ArticleDto.builder()
                .id(UUID.randomUUID())
                .title("조회수 높은 기사")
                .viewCount(100L)
                .build());

        CursorPageResponse<ArticleDto> response = CursorPageResponse.<ArticleDto>builder()
            .content(articles)
            .nextCursor("100")
            .nextAfter(Instant.now())
            .size(2)
            .totalElements(null)
            .hasNext(true)
            .build();

        when(articleService.getArticles(
            isNull(), isNull(), isNull(), isNull(), isNull(),
            eq("viewCount"), eq("ASC"), eq("5"), isNull(), eq(10)))
            .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/articles")
                .param("orderBy", "viewCount")
                .param("direction", "ASC")
                .param("cursor", "5")
                .param("limit", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content", hasSize(2)))
            .andExpect(jsonPath("$.hasNext").value(true))
            .andExpect(jsonPath("$.nextCursor").value("100"))
            .andExpect(jsonPath("$.nextAfter").isNotEmpty())
            .andExpect(jsonPath("$.size").value(2));
    }

    @Test
    void 기사_목록_조회_성공_댓글수_정렬_내림차순() throws Exception {
        // given
        List<ArticleDto> articles = Arrays.asList(
            ArticleDto.builder()
                .id(UUID.randomUUID())
                .title("댓글 많은 기사")
                .commentCount(50L)
                .build());

        CursorPageResponse<ArticleDto> response = CursorPageResponse.<ArticleDto>builder()
            .content(articles)
            .nextCursor("50")
            .nextAfter(Instant.now())
            .size(1)
            .totalElements(null)
            .hasNext(false)
            .build();

        when(articleService.getArticles(
            isNull(), isNull(), isNull(), isNull(), isNull(),
            eq("commentCount"), eq("DESC"), isNull(), isNull(), eq(10)))
            .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/articles")
                .param("orderBy", "commentCount")
                .param("direction", "DESC")
                .param("limit", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.hasNext").value(false))
            .andExpect(jsonPath("$.nextCursor").value("50"))
            .andExpect(jsonPath("$.size").value(1));
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
            .andExpect(jsonPath("$[0]").value("연합뉴스"))
            .andExpect(jsonPath("$[1]").value("중앙일보"))
            .andExpect(jsonPath("$[2]").value("한국일보"));
    }
}
