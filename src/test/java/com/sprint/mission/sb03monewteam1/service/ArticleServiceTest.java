package com.sprint.mission.sb03monewteam1.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
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

import com.sprint.mission.sb03monewteam1.dto.ArticleDto;
import com.sprint.mission.sb03monewteam1.dto.ArticleViewDto;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponseArticleDto;
import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.entity.ArticleView;
import com.sprint.mission.sb03monewteam1.exception.article.ArticleNotFoundException;
import com.sprint.mission.sb03monewteam1.exception.article.DuplicateArticleViewException;
import com.sprint.mission.sb03monewteam1.exception.common.InvalidCursorException;
import com.sprint.mission.sb03monewteam1.fixture.ArticleFixture;
import com.sprint.mission.sb03monewteam1.fixture.ArticleViewFixture;
import com.sprint.mission.sb03monewteam1.mapper.ArticleMapper;
import com.sprint.mission.sb03monewteam1.mapper.ArticleViewMapper;
import com.sprint.mission.sb03monewteam1.repository.ArticleRepository;
import com.sprint.mission.sb03monewteam1.repository.ArticleViewRepository;

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

        when(articleViewRepository.existsByUserIdAndArticleId(userId, articleId))
                .thenReturn(false);
        when(articleRepository.incrementViewCount(articleId))
                .thenReturn(1L); // 성공적으로 업데이트됨
        when(articleRepository.findByIdAndIsDeletedFalse(articleId))
                .thenReturn(Optional.of(article));
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

        verify(articleViewRepository).existsByUserIdAndArticleId(userId, articleId);
        verify(articleRepository).incrementViewCount(articleId);
        verify(articleRepository).findByIdAndIsDeletedFalse(articleId);
        verify(articleViewRepository).save(any(ArticleView.class));
    }

    @Test
    void 기사_뷰_등록_실패_기사_없음() {
        // given
        UUID userId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();

        when(articleViewRepository.existsByUserIdAndArticleId(userId, articleId))
                .thenReturn(false);
        when(articleRepository.incrementViewCount(articleId))
                .thenReturn(0L); // 업데이트 실패 (기사가 없음)

        // when & then
        assertThatThrownBy(() -> articleService.createArticleView(userId, articleId))
                .isInstanceOf(ArticleNotFoundException.class)
                .hasMessage("기사를 찾을 수 없습니다.");

        verify(articleViewRepository).existsByUserIdAndArticleId(userId, articleId);
        verify(articleRepository).incrementViewCount(articleId);
        verify(articleRepository, never()).findByIdAndIsDeletedFalse(any());
        verify(articleViewRepository, never()).save(any());
    }

    @Test
    void 기사_뷰_등록_실패_중복_조회() {
        // given
        UUID userId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();

        when(articleViewRepository.existsByUserIdAndArticleId(userId, articleId))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> articleService.createArticleView(userId, articleId))
                .isInstanceOf(DuplicateArticleViewException.class);

        verify(articleViewRepository).existsByUserIdAndArticleId(userId, articleId);
        verify(articleRepository, never()).findById(any());
        verify(articleViewRepository, never()).save(any());
    }

    @Test
    void 기사_목록_조회_성공_발행일_정렬_내림차순() {
        // given
        String keyword = "테스트";
        List<String> sourceIn = Arrays.asList("네이버뉴스");
        List<String> interests = Arrays.asList("IT", "과학");
        Instant publishDateFrom = Instant.parse("2024-01-01T00:00:00Z");
        Instant publishDateTo = Instant.parse("2024-12-31T23:59:59Z");
        String orderBy = "publishDate";
        String direction = "DESC";
        String cursor = "2024-06-01T00:00:00Z";
        int limit = 10;

        List<Article> articles = Arrays.asList(
                ArticleFixture.createArticle(),
                ArticleFixture.createArticle());

        List<ArticleDto> articleDtos = Arrays.asList(
                ArticleDto.builder().id(UUID.randomUUID()).title("제목1").build(),
                ArticleDto.builder().id(UUID.randomUUID()).title("제목2").build());

        when(articleRepository.findArticlesWithCursorByDate(
                eq(keyword), eq(sourceIn), eq(publishDateFrom), eq(publishDateTo),
                eq(Instant.parse(cursor)), eq(limit + 1), eq(false)))
                .thenReturn(articles);
        when(articleMapper.toDto(any(Article.class)))
                .thenReturn(articleDtos.get(0), articleDtos.get(1));

        // when
        CursorPageResponseArticleDto result = articleService.getArticles(
                keyword, sourceIn, interests, publishDateFrom, publishDateTo,
                orderBy, direction, cursor, null, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(2);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.size()).isEqualTo(2);

        verify(articleRepository).findArticlesWithCursorByDate(
                eq(keyword), eq(sourceIn), eq(publishDateFrom), eq(publishDateTo),
                eq(Instant.parse(cursor)), eq(limit + 1), eq(false));
    }

    @Test
    void 기사_목록_조회_성공_조회수_정렬_오름차순() {
        // given
        String orderBy = "viewCount";
        String direction = "ASC";
        String cursor = "100";
        int limit = 10;

        List<Article> articles = Arrays.asList(
                ArticleFixture.createArticle(),
                ArticleFixture.createArticle());

        List<ArticleDto> articleDtos = Arrays.asList(
                ArticleDto.builder().id(UUID.randomUUID()).title("제목1").build(),
                ArticleDto.builder().id(UUID.randomUUID()).title("제목2").build());

        when(articleRepository.findArticlesWithCursorByViewCount(
                isNull(), isNull(), isNull(), isNull(), eq(100L), any(Instant.class), eq(limit + 1), eq(true)))
                .thenReturn(articles);
        when(articleMapper.toDto(any(Article.class)))
                .thenReturn(articleDtos.get(0), articleDtos.get(1));

        // when
        CursorPageResponseArticleDto result = articleService.getArticles(
                null, null, null, null, null, orderBy, direction, cursor, null, limit);

        // then
        assertThat(result.content()).hasSize(2);
        assertThat(result.hasNext()).isFalse();

        verify(articleRepository).findArticlesWithCursorByViewCount(
                isNull(), isNull(), isNull(), isNull(), eq(100L), any(Instant.class), eq(limit + 1), eq(true));
    }

    @Test
    void 기사_목록_조회_성공_댓글수_정렬_내림차순() {
        // given
        String orderBy = "commentCount";
        String direction = "DESC";
        String cursor = "50";
        int limit = 10;

        List<Article> articles = Arrays.asList(
                ArticleFixture.createArticle(),
                ArticleFixture.createArticle());

        List<ArticleDto> articleDtos = Arrays.asList(
                ArticleDto.builder().id(UUID.randomUUID()).title("제목1").build(),
                ArticleDto.builder().id(UUID.randomUUID()).title("제목2").build());

        when(articleRepository.findArticlesWithCursorByCommentCount(
                isNull(), isNull(), isNull(), isNull(), eq(50L), any(Instant.class), eq(limit + 1), eq(false)))
                .thenReturn(articles);
        when(articleMapper.toDto(any(Article.class)))
                .thenReturn(articleDtos.get(0), articleDtos.get(1));

        // when
        CursorPageResponseArticleDto result = articleService.getArticles(
                null, null, null, null, null, orderBy, direction, cursor, null, limit);

        // then
        assertThat(result.content()).hasSize(2);
        assertThat(result.hasNext()).isFalse();

        verify(articleRepository).findArticlesWithCursorByCommentCount(
                isNull(), isNull(), isNull(), isNull(), eq(50L), any(Instant.class), eq(limit + 1), eq(false));
    }

    @Test
    void 기사_목록_조회_성공_다음_페이지_있음() {
        // given
        List<Article> articles = Arrays.asList(
                ArticleFixture.createArticle(),
                ArticleFixture.createArticle(),
                ArticleFixture.createArticle());

        when(articleRepository.findArticlesWithCursorByDate(
                isNull(), isNull(), isNull(), isNull(), isNull(), eq(3), eq(false)))
                .thenReturn(articles);
        when(articleMapper.toDto(any(Article.class)))
                .thenReturn(ArticleDto.builder().id(UUID.randomUUID()).title("제목").build());

        // when
        CursorPageResponseArticleDto result = articleService.getArticles(
                null, null, null, null, null, "publishDate", "DESC", null, null, 2);

        // then
        assertThat(result.content()).hasSize(2);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursor()).isNotNull();
        assertThat(result.nextAfter()).isNotNull();
        assertThat(result.size()).isEqualTo(2);
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

    @Test
    void 기사_목록_조회_실패_잘못된_조회수_커서_형식() {
        // given
        String orderBy = "viewCount";
        String direction = "DESC";
        String invalidCursor = "invalid-number";
        int limit = 10;

        // when & then
        assertThatThrownBy(() -> articleService.getArticles(
                null, null, null, null, null, orderBy, direction, invalidCursor, null, limit))
                .isInstanceOf(InvalidCursorException.class)
                .hasMessage(ErrorCode.INVALID_CURSOR_COUNT.getMessage(), invalidCursor);
    }

    @Test
    void 기사_목록_조회_실패_잘못된_댓글수_커서_형식() {
        // given
        String orderBy = "commentCount";
        String direction = "ASC";
        String invalidCursor = "not-a-number";
        int limit = 10;

        // when & then
        assertThatThrownBy(() -> articleService.getArticles(
                null, null, null, null, null, orderBy, direction, invalidCursor, null, limit))
                .isInstanceOf(InvalidCursorException.class)
                .hasMessage(ErrorCode.INVALID_CURSOR_COUNT.getMessage(), invalidCursor);
    }

    @Test
    void 기사_목록_조회_실패_잘못된_날짜_커서_형식() {
        // given
        String orderBy = "publishDate";
        String direction = "DESC";
        String invalidCursor = "2024-13-45T25:99:99Z"; // 잘못된 날짜 형식
        int limit = 10;

        // when & then
        assertThatThrownBy(() -> articleService.getArticles(
                null, null, null, null, null, orderBy, direction, invalidCursor, null, limit))
                .isInstanceOf(InvalidCursorException.class)
                .hasMessage(ErrorCode.INVALID_CURSOR_DATE.getMessage(), invalidCursor);
    }
}
