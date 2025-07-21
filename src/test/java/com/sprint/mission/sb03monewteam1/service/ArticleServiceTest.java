package com.sprint.mission.sb03monewteam1.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sprint.mission.sb03monewteam1.collector.NaverNewsCollector;
import com.sprint.mission.sb03monewteam1.config.metric.MonewMetrics;
import com.sprint.mission.sb03monewteam1.dto.ArticleDto;
import com.sprint.mission.sb03monewteam1.dto.ArticleViewDto;
import com.sprint.mission.sb03monewteam1.dto.CollectedArticleDto;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponse;
import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.entity.ArticleInterest;
import com.sprint.mission.sb03monewteam1.entity.ArticleView;
import com.sprint.mission.sb03monewteam1.entity.Comment;
import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.entity.InterestKeyword;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.event.ArticleViewActivityCreateEvent;
import com.sprint.mission.sb03monewteam1.event.NewArticleCollectEvent;
import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import com.sprint.mission.sb03monewteam1.exception.article.ArticleNotFoundException;
import com.sprint.mission.sb03monewteam1.exception.common.InvalidCursorException;
import com.sprint.mission.sb03monewteam1.fixture.ArticleFixture;
import com.sprint.mission.sb03monewteam1.fixture.ArticleViewFixture;
import com.sprint.mission.sb03monewteam1.fixture.CommentFixture;
import com.sprint.mission.sb03monewteam1.fixture.UserFixture;
import com.sprint.mission.sb03monewteam1.mapper.ArticleMapper;
import com.sprint.mission.sb03monewteam1.mapper.ArticleViewActivityMapper;
import com.sprint.mission.sb03monewteam1.mapper.ArticleViewMapper;
import com.sprint.mission.sb03monewteam1.repository.jpa.articleInterest.ArticleInterestRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.article.ArticleRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.articleView.ArticleViewRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.commentLike.CommentLikeRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.comment.CommentRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.interest.InterestKeywordRepository;
import io.micrometer.core.instrument.Counter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

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

    @Mock
    private ArticleViewActivityMapper articleViewActivityMapper;

    @Mock
    private ArticleInterestRepository articleInterestRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentLikeRepository commentLikeRepository;

    @Mock
    private InterestKeywordRepository interestKeywordRepository;

    @Mock
    private NaverNewsCollector naverNewsCollector;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private MonewMetrics monewMetrics;

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

        when(articleViewRepository.findByUserIdAndArticleId(userId, articleId))
            .thenReturn(List.of());
        when(articleRepository.incrementViewCount(articleId))
            .thenReturn(1L);
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

        verify(articleViewRepository).findByUserIdAndArticleId(userId, articleId);
        verify(articleRepository).incrementViewCount(articleId);
        verify(articleRepository).findByIdAndIsDeletedFalse(articleId);
        verify(articleViewRepository).save(any(ArticleView.class));
        verify(eventPublisher).publishEvent(any(ArticleViewActivityCreateEvent.class));
    }

    @Test
    void 기사_뷰_등록_실패_기사_없음() {
        // given
        UUID userId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();

        when(articleViewRepository.findByUserIdAndArticleId(userId, articleId))
            .thenReturn(List.of());
        when(articleRepository.incrementViewCount(articleId))
            .thenReturn(0L);

        // when & then
        assertThatThrownBy(() -> articleService.createArticleView(userId, articleId))
            .isInstanceOf(ArticleNotFoundException.class)
            .hasMessage("기사를 찾을 수 없습니다.");

        verify(articleViewRepository).findByUserIdAndArticleId(userId, articleId);
        verify(articleRepository).incrementViewCount(articleId);
        verify(articleRepository, never()).findByIdAndIsDeletedFalse(any());
        verify(articleViewRepository, never()).save(any());
    }

    @Test
    void 기사_뷰_등록_중복_조회시_기존_뷰_반환() {
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

        when(articleViewRepository.findByUserIdAndArticleId(userId, articleId))
            .thenReturn(List.of(articleView));
        when(articleViewMapper.toDto(articleView))
            .thenReturn(expectedDto);

        // when
        ArticleViewDto result = articleService.createArticleView(userId, articleId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.articleId()).isEqualTo(articleId);

        verify(articleViewRepository).findByUserIdAndArticleId(userId, articleId);
        verify(articleRepository, never()).incrementViewCount(any());
        verify(articleRepository, never()).findByIdAndIsDeletedFalse(any());
        verify(articleViewRepository, never()).save(any());
    }

    @Test
    void 기사_목록_조회_성공_발행일_정렬_내림차순() {
        // given
        String keyword = "테스트";
        List<String> sourceIn = Arrays.asList("네이버뉴스");
        List<String> interests = Arrays.asList("IT", "과학");
        String publishDateFrom = "2024-01-01T00:00:00";
        String publishDateTo = "2024-12-31T23:59:59";
        Instant publishDateFromInstant = LocalDateTime.parse(publishDateFrom)
            .atZone(ZoneId.of("Asia/Seoul")).toInstant();
        Instant publishDateToInstant = LocalDateTime.parse(publishDateTo)
            .atZone(ZoneId.of("Asia/Seoul")).toInstant();
        String orderBy = "publishDate";
        String direction = "DESC";
        String cursor = "2024-06-01T00:00:00Z";
        Instant after = Instant.parse(cursor);
        int limit = 10;

        List<Article> articles = Arrays.asList(
            ArticleFixture.createArticle(),
            ArticleFixture.createArticle());

        List<ArticleDto> articleDtos = Arrays.asList(
            ArticleDto.builder().id(UUID.randomUUID()).title("제목1").build(),
            ArticleDto.builder().id(UUID.randomUUID()).title("제목2").build());

        when(articleRepository.findArticlesWithCursorByDate(
            eq(keyword), eq(sourceIn), eq(publishDateFromInstant),
            eq(publishDateToInstant),
            eq(after), eq(limit + 1), eq(false)))
            .thenReturn(articles);
        when(articleMapper.toDto(any(Article.class)))
            .thenReturn(articleDtos.get(0), articleDtos.get(1));

        // when
        CursorPageResponse<ArticleDto> result = articleService.getArticles(
            keyword, sourceIn, interests, publishDateFrom, publishDateTo,
            orderBy, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(2);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.size()).isEqualTo(2);

        verify(articleRepository).findArticlesWithCursorByDate(
            eq(keyword), eq(sourceIn), eq(publishDateFromInstant),
            eq(publishDateToInstant),
            eq(after), eq(limit + 1), eq(false));
    }

    @Test
    void 기사_목록_조회_성공_조회수_정렬_오름차순() {
        // given
        String orderBy = "viewCount";
        String direction = "ASC";
        String cursor = "100";
        Instant after = Instant.parse("2024-06-08T09:00:00.484468Z");
        int limit = 10;

        List<Article> articles = Arrays.asList(
            ArticleFixture.createArticle(),
            ArticleFixture.createArticle());

        List<ArticleDto> articleDtos = Arrays.asList(
            ArticleDto.builder().id(UUID.randomUUID()).title("제목1").build(),
            ArticleDto.builder().id(UUID.randomUUID()).title("제목2").build());

        when(articleRepository.findArticlesWithCursorByViewCount(
            any(), any(), any(), any(), anyLong(), any(Instant.class), anyInt(), anyBoolean()))
            .thenReturn(articles);
        when(articleMapper.toDto(any(Article.class)))
            .thenReturn(articleDtos.get(0), articleDtos.get(1));

        // when
        CursorPageResponse<ArticleDto> result = articleService.getArticles(
            null, null, null, null, null, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result.content()).hasSize(2);
        assertThat(result.hasNext()).isFalse();

        verify(articleRepository).findArticlesWithCursorByViewCount(
            isNull(), isNull(), isNull(), isNull(), eq(100L), any(Instant.class), eq(limit + 1),
            eq(true));
    }

    @Test
    void 기사_목록_조회_성공_댓글수_정렬_내림차순() {
        // given
        String orderBy = "commentCount";
        String direction = "DESC";
        String cursor = "50";
        Instant after = Instant.parse("2024-06-08T09:00:00.484468Z");
        int limit = 10;

        List<Article> articles = Arrays.asList(
            ArticleFixture.createArticle(),
            ArticleFixture.createArticle());

        List<ArticleDto> articleDtos = Arrays.asList(
            ArticleDto.builder().id(UUID.randomUUID()).title("제목1").build(),
            ArticleDto.builder().id(UUID.randomUUID()).title("제목2").build());

        when(articleRepository.findArticlesWithCursorByCommentCount(
            isNull(), isNull(), isNull(), isNull(), eq(50L), any(Instant.class), eq(limit + 1),
            eq(false)))
            .thenReturn(articles);
        when(articleMapper.toDto(any(Article.class)))
            .thenReturn(articleDtos.get(0), articleDtos.get(1));

        // when
        CursorPageResponse<ArticleDto> result = articleService.getArticles(
            null, null, null, null, null, orderBy, direction, cursor, after, limit);

        // then
        assertThat(result.content()).hasSize(2);
        assertThat(result.hasNext()).isFalse();

        verify(articleRepository).findArticlesWithCursorByCommentCount(
            isNull(), isNull(), isNull(), isNull(), eq(50L), any(Instant.class), eq(limit + 1),
            eq(false));
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
        CursorPageResponse<ArticleDto> result = articleService.getArticles(
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
        String invalidCursor = "2024-13-45T25:99:99Z";
        int limit = 10;

        // when & then
        assertThatThrownBy(() -> articleService.getArticles(
            null, null, null, null, null, orderBy, direction, invalidCursor, null, limit))
            .isInstanceOf(InvalidCursorException.class)
            .hasMessage(ErrorCode.INVALID_CURSOR_DATE.getMessage(), invalidCursor);
    }

    @Test
    void 네이버_뉴스_수집시_ArticleInterest_저장_확인() {
        // given
        Interest interest1 = Interest.builder().name("IT").build();
        Interest interest2 = Interest.builder().name("SPORTS").build();
        String keyword = "테스트";
        InterestKeyword ik1 = InterestKeyword.builder().interest(interest1).keyword(keyword)
            .build();
        InterestKeyword ik2 = InterestKeyword.builder().interest(interest2).keyword(keyword)
            .build();
        CollectedArticleDto dto = CollectedArticleDto.builder()
            .source("네이버뉴스")
            .sourceUrl("http://test.com/1")
            .title("테스트 기사")
            .publishDate(Instant.now())
            .summary("요약")
            .build();
        List<CollectedArticleDto> collectedArticles = List.of(dto);
        Article article = ArticleFixture.createArticle();

        when(naverNewsCollector.collect(keyword)).thenReturn(collectedArticles);
        when(articleMapper.toEntity(dto)).thenReturn(article);
        when(articleRepository.findAllBySourceUrlIn(anyList())).thenReturn(List.of());
        when(articleRepository.saveAll(anyList())).thenReturn(List.of(article));
        when(interestKeywordRepository.findAllByKeyword(keyword)).thenReturn(List.of(ik1, ik2));
        when(monewMetrics.getArticleCreatedCounter()).thenReturn(
            mock(io.micrometer.core.instrument.Counter.class));
        when(monewMetrics.getInterestArticleMappedCounter(any(), any())).thenReturn(
            mock(Counter.class));

        List<Article> articles = articleService.collectNaverArticles(keyword);
        articleService.saveArticles(articles, keyword);

        // then
        verify(articleRepository).saveAll(anyList());
        verify(articleInterestRepository, times(2)).saveAll(anyList());
    }

    @Test
    void 기사_논리_삭제_시_flag_변경() {
        UUID articleId = UUID.randomUUID();
        Article article = ArticleFixture.createArticleWithId(articleId);
        when(articleRepository.findByIdAndIsDeletedFalse(articleId)).thenReturn(
            Optional.of(article));

        articleService.delete(articleId);

        assertThat(article.isDeleted()).isTrue();
        verify(articleRepository).findByIdAndIsDeletedFalse(articleId);
    }

    @Test
    void 존재하지_않는_기사_논리_삭제_요청시_예외() {
        UUID articleId = UUID.randomUUID();
        when(articleRepository.findByIdAndIsDeletedFalse(articleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.delete(articleId))
            .isInstanceOf(ArticleNotFoundException.class);

        verify(articleRepository).findByIdAndIsDeletedFalse(articleId);
    }

    @Test
    void 기사_물리_삭제시_연관데이터_모두_삭제() {
        UUID articleId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();

        Article article = ArticleFixture.createArticleWithId(articleId);

        User user = UserFixture.createUser();
        Comment comment = CommentFixture.createCommentWithId(commentId, user, article);

        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        when(commentRepository.findAllByArticleId(articleId)).thenReturn(List.of(comment));

        articleService.deleteHard(articleId);

        verify(commentRepository).findAllByArticleId(articleId);
        verify(commentLikeRepository).deleteByCommentId(commentId);
        verify(commentRepository).deleteById(commentId);
        verify(articleViewRepository).deleteByArticleId(articleId);
        verify(articleInterestRepository).deleteByArticleId(articleId);
        verify(articleRepository).deleteById(articleId);
    }

    @Test
    void 존재하지_않는_기사_물리_삭제_요청시_예외() {
        UUID articleId = UUID.randomUUID();
        when(articleRepository.findById(articleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.deleteHard(articleId))
            .isInstanceOf(ArticleNotFoundException.class);

        verify(articleRepository).findById(articleId);
    }

    @Test
    void 기사_중복_저장_실패_이미_존재하는_기사() {
        // given
        String keyword = "테스트";
        CollectedArticleDto dto = CollectedArticleDto.builder()
            .source("네이버뉴스")
            .sourceUrl("http://test.com/1")
            .title("테스트 기사")
            .publishDate(Instant.now())
            .summary("요약")
            .build();
        when(naverNewsCollector.collect(keyword)).thenReturn(List.of(dto));
        when(articleRepository.findAllBySourceUrlIn(anyList())).thenReturn(
            List.of(dto.sourceUrl()));

        // when
        List<Article> articles = articleService.collectNaverArticles(keyword);

        // then
        assertThat(articles).isEmpty();
        verify(articleRepository).findAllBySourceUrlIn(anyList());
        verify(articleRepository, never()).saveAll(anyList());
        verify(articleInterestRepository, never()).save(any(ArticleInterest.class));
    }
}
