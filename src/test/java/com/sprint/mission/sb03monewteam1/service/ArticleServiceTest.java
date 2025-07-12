package com.sprint.mission.sb03monewteam1.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sprint.mission.sb03monewteam1.dto.ArticleDto;
import com.sprint.mission.sb03monewteam1.dto.ArticleViewDto;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponseArticleDto;
import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.entity.ArticleView;
import com.sprint.mission.sb03monewteam1.exception.article.ArticleNotFoundException;
import com.sprint.mission.sb03monewteam1.exception.article.DuplicateArticleViewException;
import com.sprint.mission.sb03monewteam1.fixture.ArticleFixture;
import com.sprint.mission.sb03monewteam1.fixture.ArticleViewFixture;
import com.sprint.mission.sb03monewteam1.mapper.ArticleMapper;
import com.sprint.mission.sb03monewteam1.mapper.ArticleViewMapper;
import com.sprint.mission.sb03monewteam1.repository.ArticleRepository;
import com.sprint.mission.sb03monewteam1.repository.ArticleViewRepository;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ArticleViewRepository articleViewRepository;

    @Mock
    private ArticleMapper articleMapper;

    @Mock
    private ArticleViewMapper articleViewMapper;

    @InjectMocks
    private ArticleServiceImpl articleService;

    @Test
    void 기사_뷰_등록_성공() {
        // given
        UUID userId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();
        Article article = ArticleFixture.createArticleWithId(articleId);
        ArticleView articleView = ArticleViewFixture.createArticleView(userId, article);
        ArticleViewDto expectedDto = ArticleViewDto.builder()
            .id(articleView.getId())
            .userId(userId)
            .articleId(articleId)
            .createdAt(articleView.getCreatedAt())
            .build();

        when(articleRepository.findByIdAndIsDeletedFalse(articleId))
            .thenReturn(Optional.of(article));
        when(articleViewRepository.existsByUserIdAndArticleId(userId, articleId))
            .thenReturn(false);
        when(articleViewRepository.save(any(ArticleView.class)))
            .thenReturn(articleView);
        when(articleViewMapper.toDto(articleView))
            .thenReturn(expectedDto);

        // when
        ArticleViewDto result = articleService.createArticleView(userId, articleId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.articleId()).isEqualTo(articleId);

        verify(articleRepository).findByIdAndIsDeletedFalse(articleId);
        verify(articleViewRepository).existsByUserIdAndArticleId(userId, articleId);
        verify(articleViewRepository).save(any(ArticleView.class));
        verify(articleRepository).save(article); // 조회수 증가
    }

    @Test
    void 기사_뷰_등록_실패_기사_없음() {
        // given
        UUID userId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();

        when(articleRepository.findByIdAndIsDeletedFalse(articleId))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> articleService.createArticleView(userId, articleId))
            .isInstanceOf(ArticleNotFoundException.class);

        verify(articleRepository).findByIdAndIsDeletedFalse(articleId);
        verify(articleViewRepository, never()).existsByUserIdAndArticleId(any(), any());
        verify(articleViewRepository, never()).save(any());
    }

    @Test
    void 기사_뷰_등록_실패_중복_조회() {
        // given
        UUID userId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();
        Article article = ArticleFixture.createArticleWithId(articleId);

        when(articleRepository.findByIdAndIsDeletedFalse(articleId))
            .thenReturn(Optional.of(article));
        when(articleViewRepository.existsByUserIdAndArticleId(userId, articleId))
            .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> articleService.createArticleView(userId, articleId))
            .isInstanceOf(DuplicateArticleViewException.class);

        verify(articleRepository).findByIdAndIsDeletedFalse(articleId);
        verify(articleViewRepository).existsByUserIdAndArticleId(userId, articleId);
        verify(articleViewRepository, never()).save(any());
    }

    @Test
    void 기사_목록_조회_성공_날짜_정렬() {
        // given
        String searchKeyword = "테스트";
        String source = "네이버뉴스";
        List<String> interests = Arrays.asList("IT", "과학");
        Instant startDate = Instant.parse("2024-01-01T00:00:00Z");
        Instant endDate = Instant.parse("2024-12-31T23:59:59Z");
        String sortBy = "publishDate";
        String cursor = "2024-06-01T00:00:00Z";
        int limit = 10;

        List<Article> articles = Arrays.asList(
            ArticleFixture.createArticle(),
            ArticleFixture.createArticle());

        List<ArticleDto> articleDtos = Arrays.asList(
            ArticleDto.builder().id(UUID.randomUUID()).title("제목1").build(),
            ArticleDto.builder().id(UUID.randomUUID()).title("제목2").build());

        when(articleRepository.findArticlesWithCursorByDate(
            searchKeyword, source, startDate, endDate, Instant.parse(cursor), limit + 1))
            .thenReturn(articles);
        when(articleMapper.toDto(any(Article.class)))
            .thenReturn(articleDtos.get(0), articleDtos.get(1));

        // when
        CursorPageResponseArticleDto result = articleService.getArticles(
            searchKeyword, source, interests, startDate, endDate, sortBy, cursor, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.articles()).hasSize(2);
        assertThat(result.hasNext()).isFalse();

        verify(articleRepository).findArticlesWithCursorByDate(
            searchKeyword, source, startDate, endDate, Instant.parse(cursor), limit + 1);
    }

    @Test
    void 기사_목록_조회_성공_조건_없음() {
        // given
        List<Article> articles = Arrays.asList(
            ArticleFixture.createArticle(),
            ArticleFixture.createArticle());

        List<ArticleDto> articleDtos = Arrays.asList(
            ArticleDto.builder().id(UUID.randomUUID()).title("제목1").build(),
            ArticleDto.builder().id(UUID.randomUUID()).title("제목2").build());

        when(articleRepository.findArticlesWithCursorByDate(any(), any(), any(), any(), any(),
            anyInt()))
            .thenReturn(articles);
        when(articleMapper.toDto(any(Article.class)))
            .thenReturn(articleDtos.get(0), articleDtos.get(1));

        // when
        CursorPageResponseArticleDto result = articleService.getArticles(
            null, null, null, null, null, "publishDate", null, 10);

        // then
        assertThat(result.articles()).hasSize(2);
        assertThat(result.hasNext()).isFalse();

        verify(articleRepository).findArticlesWithCursorByDate(
            isNull(), isNull(), isNull(), isNull(), isNull(), eq(11));
    }

    @Test
    void 기사_목록_조회_성공_조회수_정렬() {
        // given
        List<Article> articles = Arrays.asList(
            ArticleFixture.createArticle(),
            ArticleFixture.createArticle());

        List<ArticleDto> articleDtos = Arrays.asList(
            ArticleDto.builder().id(UUID.randomUUID()).title("제목1").build(),
            ArticleDto.builder().id(UUID.randomUUID()).title("제목2").build());

        when(articleRepository.findArticlesWithCursorByViewCount(any(), any(), any(), any(), any(),
            anyInt()))
            .thenReturn(articles);
        when(articleMapper.toDto(any(Article.class)))
            .thenReturn(articleDtos.get(0), articleDtos.get(1));

        // when
        CursorPageResponseArticleDto result = articleService.getArticles(
            null, null, null, null, null, "viewCount", null, 10);

        // then
        assertThat(result.articles()).hasSize(2);
        assertThat(result.hasNext()).isFalse();

        verify(articleRepository).findArticlesWithCursorByViewCount(
            isNull(), isNull(), isNull(), isNull(), isNull(), eq(11));
    }

    @Test
    void 기사_목록_조회_성공_다음_페이지_있음() {
        // given
        List<Article> articles = Arrays.asList(
            ArticleFixture.createArticle(),
            ArticleFixture.createArticle(),
            ArticleFixture.createArticle());

        List<ArticleDto> articleDtos = Arrays.asList(
            ArticleDto.builder().id(UUID.randomUUID()).title("제목1").build(),
            ArticleDto.builder().id(UUID.randomUUID()).title("제목2").build(),
            ArticleDto.builder().id(UUID.randomUUID()).title("제목3").build());

        when(articleRepository.findArticlesWithCursorByDate(any(), any(), any(), any(), any(),
            anyInt()))
            .thenReturn(articles);
        when(articleMapper.toDto(any(Article.class)))
            .thenReturn(articleDtos.get(0), articleDtos.get(1), articleDtos.get(2));

        // when
        CursorPageResponseArticleDto result = articleService.getArticles(
            null, null, null, null, null, "publishDate", null, 2);

        // then
        assertThat(result.articles()).hasSize(2); // limit만큼만 반환
        assertThat(result.hasNext()).isTrue(); // 다음 페이지 있음
        assertThat(result.nextCursor()).isNotNull(); // 다음 커서 있음

        verify(articleRepository).findArticlesWithCursorByDate(
            isNull(), isNull(), isNull(), isNull(), isNull(), eq(3));
    }

    @Test
    void 기사_출처_목록_조회_성공() {
        // given
        List<String> sources = Arrays.asList("연합뉴스", "조선일보", "한국일보");
        when(articleRepository.findDistinctSources()).thenReturn(sources);

        // when
        List<String> result = articleService.getSources();

        // then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly("연합뉴스", "조선일보", "한국일보");

        verify(articleRepository).findDistinctSources();
    }
}
