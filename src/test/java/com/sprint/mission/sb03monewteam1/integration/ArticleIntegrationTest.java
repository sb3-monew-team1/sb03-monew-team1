package com.sprint.mission.sb03monewteam1.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.sb03monewteam1.config.LoadTestEnv;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponseArticleDto;
import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.repository.ArticleRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@LoadTestEnv
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ArticleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ArticleRepository articleRepository;

    private Article testArticle1;
    private Article testArticle2;
    private Article testArticle3;
    private Article testArticle4;

    @BeforeEach
    void setUp() {

        testArticle1 = Article.builder()
            .source("네이버뉴스")
            .sourceUrl("https://n.news.naver.com/mnews/article/001/001")
            .title("코로나 확진자 급증")
            .summary("코로나19 확진자가 급증하고 있습니다")
            .viewCount(100L)
            .commentCount(5L)
            .publishDate(Instant.parse("2024-01-01T00:00:00Z"))
            .isDeleted(false)
            .build();

        testArticle2 = Article.builder()
            .source("조선일보")
            .sourceUrl("https://www.chosun.com/arc/outboundfeeds/rss/002/002")
            .title("경제 성장률 발표")
            .summary("올해 경제 성장률이 발표되었습니다")
            .viewCount(200L)
            .commentCount(10L)
            .publishDate(Instant.parse("2024-01-02T00:00:00Z"))
            .isDeleted(false)
            .build();

        testArticle3 = Article.builder()
            .source("한국일보")
            .sourceUrl("https://www.hankyung.com/feed/003/003")
            .title("코로나 백신 접종률")
            .summary("전국 코로나 백신 접종률이 80%를 넘었습니다")
            .viewCount(50L)
            .commentCount(15L)
            .publishDate(Instant.parse("2024-01-03T00:00:00Z"))
            .isDeleted(false)
            .build();

        testArticle4 = Article.builder()
            .source("중앙일보")
            .sourceUrl("https://www.joongang.co.kr/feed/004/004")
            .title("기술 발전 현황")
            .summary("최신 기술 발전 현황을 알아봅시다")
            .viewCount(150L)
            .commentCount(3L)
            .publishDate(Instant.parse("2024-01-04T00:00:00Z"))
            .isDeleted(false)
            .build();

        testArticle1 = articleRepository.save(testArticle1);
        testArticle2 = articleRepository.save(testArticle2);
        testArticle3 = articleRepository.save(testArticle3);
        testArticle4 = articleRepository.save(testArticle4);
    }

    @Test
    void 기사_목록_조회_발행일_정렬_내림차순() throws Exception {
        // when & then
        mockMvc.perform(get("/api/articles")
                .param("orderBy", "publishDate")
                .param("direction", "DESC")
                .param("limit", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content", hasSize(4)))
            .andExpect(jsonPath("$.content[0].title").value("기술 발전 현황"))
            .andExpect(jsonPath("$.content[1].title").value("코로나 백신 접종률"))
            .andExpect(jsonPath("$.content[2].title").value("경제 성장률 발표"))
            .andExpect(jsonPath("$.content[3].title").value("코로나 확진자 급증"))
            .andExpect(jsonPath("$.hasNext").value(false))
            .andExpect(jsonPath("$.size").value(4));
    }

    @Test
    void 기사_목록_조회_발행일_정렬_오름차순() throws Exception {
        // when & then
        mockMvc.perform(get("/api/articles")
                .param("orderBy", "publishDate")
                .param("direction", "ASC")
                .param("limit", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content", hasSize(4)))
            .andExpect(jsonPath("$.content[0].title").value("코로나 확진자 급증"))
            .andExpect(jsonPath("$.content[1].title").value("경제 성장률 발표"))
            .andExpect(jsonPath("$.content[2].title").value("코로나 백신 접종률"))
            .andExpect(jsonPath("$.content[3].title").value("기술 발전 현황"))
            .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void 기사_목록_조회_조회수_정렬_내림차순() throws Exception {
        // when & then
        mockMvc.perform(get("/api/articles")
                .param("orderBy", "viewCount")
                .param("direction", "DESC")
                .param("limit", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content", hasSize(4)))
            .andExpect(jsonPath("$.content[0].title").value("경제 성장률 발표"))
            .andExpect(jsonPath("$.content[1].title").value("기술 발전 현황"))
            .andExpect(jsonPath("$.content[2].title").value("코로나 확진자 급증"))
            .andExpect(jsonPath("$.content[3].title").value("코로나 백신 접종률"))
            .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void 기사_목록_조회_댓글수_정렬_내림차순() throws Exception {
        // when & then
        mockMvc.perform(get("/api/articles")
                .param("orderBy", "commentCount")
                .param("direction", "DESC")
                .param("limit", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content", hasSize(4)))
            .andExpect(jsonPath("$.content[0].title").value("코로나 백신 접종률"))
            .andExpect(jsonPath("$.content[1].title").value("경제 성장률 발표"))
            .andExpect(jsonPath("$.content[2].title").value("코로나 확진자 급증"))
            .andExpect(jsonPath("$.content[3].title").value("기술 발전 현황"))
            .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void 커서_페이지네이션_통합_테스트_발행일_정렬() throws Exception {
        // 1. 첫 번째 페이지 조회 (limit: 2)
        MvcResult firstPageResult = mockMvc.perform(get("/api/articles")
                .param("orderBy", "publishDate")
                .param("direction", "DESC")
                .param("limit", "2")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        CursorPageResponseArticleDto firstPageResponse = objectMapper.readValue(
            firstPageResult.getResponse().getContentAsString(),
            CursorPageResponseArticleDto.class);

        assertThat(firstPageResponse.content()).hasSize(2);
        assertThat(firstPageResponse.hasNext()).isTrue();
        assertThat(firstPageResponse.nextCursor()).isNotNull();
        assertThat(firstPageResponse.nextAfter()).isNotNull();
        assertThat(firstPageResponse.size()).isEqualTo(2);

        // 2. 두 번째 페이지 조회 (cursor 사용)
        MvcResult secondPageResult = mockMvc.perform(get("/api/articles")
                .param("orderBy", "publishDate")
                .param("direction", "DESC")
                .param("cursor", firstPageResponse.nextCursor())
                .param("limit", "2")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        CursorPageResponseArticleDto secondPageResponse = objectMapper.readValue(
            secondPageResult.getResponse().getContentAsString(),
            CursorPageResponseArticleDto.class);

        assertThat(secondPageResponse.content()).hasSize(2);
        assertThat(secondPageResponse.hasNext()).isFalse();
        assertThat(secondPageResponse.nextCursor()).isNull();
        assertThat(secondPageResponse.size()).isEqualTo(2);
    }

    @Test
    void 기사_검색_기능_통합_테스트() throws Exception {
        // 1. 키워드 검색
        MvcResult searchResult = mockMvc.perform(get("/api/articles")
                .param("keyword", "코로나")
                .param("orderBy", "publishDate")
                .param("direction", "DESC")
                .param("limit", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        CursorPageResponseArticleDto searchResponse = objectMapper.readValue(
            searchResult.getResponse().getContentAsString(),
            CursorPageResponseArticleDto.class);

        assertThat(searchResponse.content()).hasSize(2);

        // 2. 출처 필터링
        MvcResult sourceResult = mockMvc.perform(get("/api/articles")
                .param("sourceIn", "네이버뉴스", "조선일보")
                .param("orderBy", "publishDate")
                .param("direction", "DESC")
                .param("limit", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        CursorPageResponseArticleDto sourceResponse = objectMapper.readValue(
            sourceResult.getResponse().getContentAsString(),
            CursorPageResponseArticleDto.class);

        assertThat(sourceResponse.content()).hasSize(2);
    }

    @Test
    void 기사_출처_목록_조회_통합_테스트() throws Exception {
        // when & then
        MvcResult result = mockMvc.perform(get("/api/articles/sources")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        @SuppressWarnings("unchecked")
        List<String> sources = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            List.class);

        assertThat(sources).hasSize(4);
        assertThat(sources).containsExactlyInAnyOrder("네이버뉴스", "조선일보", "한국일보", "중앙일보");
    }
}