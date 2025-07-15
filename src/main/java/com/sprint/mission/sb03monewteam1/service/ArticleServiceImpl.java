package com.sprint.mission.sb03monewteam1.service;

import com.sprint.mission.sb03monewteam1.collector.HankyungNewsCollector;
import com.sprint.mission.sb03monewteam1.collector.NaverNewsCollector;
import com.sprint.mission.sb03monewteam1.dto.ArticleDto;
import com.sprint.mission.sb03monewteam1.dto.ArticleViewDto;
import com.sprint.mission.sb03monewteam1.dto.CollectedArticleDto;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponse;
import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.entity.ArticleView;
import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import com.sprint.mission.sb03monewteam1.exception.article.ArticleNotFoundException;
import com.sprint.mission.sb03monewteam1.exception.article.DuplicateArticleViewException;
import com.sprint.mission.sb03monewteam1.exception.common.InvalidCursorException;
import com.sprint.mission.sb03monewteam1.mapper.ArticleMapper;
import com.sprint.mission.sb03monewteam1.mapper.ArticleViewMapper;
import com.sprint.mission.sb03monewteam1.repository.ArticleRepository;
import com.sprint.mission.sb03monewteam1.repository.ArticleViewRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleViewRepository articleViewRepository;
    private final ArticleMapper articleMapper;
    private final ArticleViewMapper articleViewMapper;
    private final NaverNewsCollector naverNewsCollector;
    private final HankyungNewsCollector hankyungNewsCollector;

    @Override
    @Transactional
    public ArticleViewDto createArticleView(UUID userId, UUID articleId) {
        if (articleViewRepository.existsByUserIdAndArticleId(userId, articleId)) {
            throw new DuplicateArticleViewException();
        }

        long updated = articleRepository.incrementViewCount(articleId);
        if (updated == 0) {
            throw new ArticleNotFoundException(articleId.toString());
        }

        Article article = articleRepository.findByIdAndIsDeletedFalse(articleId)
            .orElseThrow(() -> new ArticleNotFoundException(articleId.toString()));

        ArticleView articleView = ArticleView.createArticleView(userId, article);
        ArticleView savedArticleView = articleViewRepository.save(articleView);

        ArticleViewDto result = articleViewMapper.toDto(savedArticleView);
        log.info("기사 뷰 등록 완료 - id: {}", result.id());

        return result;
    }

    private Instant parseToKstInstant(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        DateTimeFormatter formatter = value.contains(".")
            ? DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
            : DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime ldt = LocalDateTime.parse(value, formatter);
        return ldt.atZone(ZoneId.of("Asia/Seoul")).toInstant();
    }

    @Override
    public CursorPageResponse<ArticleDto> getArticles(
        String keyword,
        List<String> sourceIn,
        List<String> interests,
        String publishDateFrom,
        String publishDateTo,
        String orderBy,
        String direction,
        String cursor,
        Instant after,
        int limit) {

        Instant from = parseToKstInstant(publishDateFrom);
        Instant to = parseToKstInstant(publishDateTo);

        String sortBy = orderBy != null ? orderBy : "publishDate";
        boolean isAscending = "ASC".equalsIgnoreCase(direction);

        List<Article> articles = getArticlesBySortType(
            keyword, sourceIn, from, to,
            sortBy, isAscending, cursor, limit);

        boolean hasNext = articles.size() > limit;
        if (hasNext) {
            articles = articles.subList(0, limit);
        }

        List<ArticleDto> articleDtos = articles.stream()
            .map(articleMapper::toDto)
            .collect(Collectors.toList());

        String nextCursor = generateNextCursor(articles, sortBy, hasNext);
        Instant nextAfter = generateNextAfter(articles, hasNext);

        CursorPageResponse<ArticleDto> result = CursorPageResponse.<ArticleDto>builder()
            .content(articleDtos)
            .nextCursor(nextCursor)
            .nextAfter(nextAfter)
            .size(articleDtos.size())
            .totalElements(null)
            .hasNext(hasNext)
            .build();

        log.info("기사 목록 조회 완료 - 조회된 기사 수: {}, hasNext: {}", articleDtos.size(), hasNext);
        return result;
    }

    private List<Article> getArticlesBySortType(
        String keyword, List<String> sourceIn, Instant publishDateFrom, Instant publishDateTo,
        String sortBy, boolean isAscending, String cursor, int limit) {

        switch (sortBy) {
            case "viewCount":
                Long viewCountCursor = null;
                if (cursor != null) {
                    try {
                        viewCountCursor = Long.valueOf(cursor);
                    } catch (NumberFormatException e) {
                        throw new InvalidCursorException(ErrorCode.INVALID_CURSOR_COUNT, cursor);
                    }
                }
                Instant viewCountPublishDate = Instant.now();
                return articleRepository.findArticlesWithCursorByViewCount(
                    keyword, sourceIn, publishDateFrom, publishDateTo,
                    viewCountCursor, viewCountPublishDate, limit + 1, isAscending);

            case "commentCount":
                Long commentCountCursor = null;
                if (cursor != null) {
                    try {
                        commentCountCursor = Long.valueOf(cursor);
                    } catch (NumberFormatException e) {
                        throw new InvalidCursorException(ErrorCode.INVALID_CURSOR_COUNT, cursor);
                    }
                }
                Instant commentCountPublishDate = Instant.now();
                return articleRepository.findArticlesWithCursorByCommentCount(
                    keyword, sourceIn, publishDateFrom, publishDateTo,
                    commentCountCursor, commentCountPublishDate, limit + 1, isAscending);

            case "publishDate":
            default:
                Instant dateCursor = null;
                if (cursor != null) {
                    try {
                        dateCursor = Instant.parse(cursor);
                    } catch (DateTimeParseException e) {
                        throw new InvalidCursorException(ErrorCode.INVALID_CURSOR_DATE, cursor);
                    }
                }
                return articleRepository.findArticlesWithCursorByDate(
                    keyword, sourceIn, publishDateFrom, publishDateTo, dateCursor, limit + 1,
                    isAscending);
        }
    }

    private String generateNextCursor(List<Article> articles, String sortBy, boolean hasNext) {
        if (!hasNext || articles.isEmpty()) {
            return null;
        }

        Article lastArticle = articles.get(articles.size() - 1);

        switch (sortBy) {
            case "viewCount":
                return lastArticle.getViewCount() + ":" + lastArticle.getPublishDate().toString();
            case "commentCount":
                return lastArticle.getCommentCount() + ":" + lastArticle.getPublishDate()
                    .toString();
            case "publishDate":
            default:
                return lastArticle.getPublishDate().toString();
        }
    }

    private Instant generateNextAfter(List<Article> articles, boolean hasNext) {
        if (!hasNext || articles.isEmpty()) {
            return null;
        }

        Article lastArticle = articles.get(articles.size() - 1);
        return lastArticle.getPublishDate();
    }

    @Override
    public List<String> getSources() {
        log.info("기사 출처 목록 조회");
        return articleRepository.findDistinctSources();
    }

    @Override
    @Transactional
    public void collectAndSaveNaverArticles(Interest interest, String keyword) {
        log.info("네이버 기사 수집 시작: 관심사={}, 키워드={}", interest.getName(), keyword);
        List<CollectedArticleDto> collectedArticles = naverNewsCollector.collect(interest, keyword);
        saveCollectedArticles(collectedArticles, interest, keyword);
    }

    @Override
    @Transactional
    public void collectAndSaveHankyungArticles(Interest interest, String keyword) {
        log.info("한국경제 기사 수집 시작: 관심사={}, 키워드={}", interest.getName(), keyword);
        List<CollectedArticleDto> collectedArticles = hankyungNewsCollector.collect(interest,
            keyword);
        saveCollectedArticles(collectedArticles, interest, keyword);
    }

    private boolean shouldIncludeArticle(CollectedArticleDto dto, String keyword) {
        String kw = keyword.toLowerCase();
        return (dto.title() != null && dto.title().toLowerCase().contains(kw))
            || (dto.summary() != null && dto.summary().toLowerCase().contains(kw));
    }

    private Article createArticleFromDto(CollectedArticleDto dto) {
        return Article.builder()
            .source(dto.source())
            .sourceUrl(dto.sourceUrl())
            .title(dto.title())
            .publishDate(dto.publishDate())
            .summary(dto.summary())
            .viewCount(0L)
            .commentCount(0L)
            .isDeleted(false)
            .build();
    }

    private void saveCollectedArticles(List<CollectedArticleDto> collectedArticles,
        Interest interest,
        String keyword) {
        List<Article> filtered = new ArrayList<>();
        for (CollectedArticleDto dto : collectedArticles) {
            if (!shouldIncludeArticle(dto, keyword)) {
                continue;
            }

            if (articleRepository.existsBySourceUrl(dto.sourceUrl())) {
                log.info("중복 기사: {}", dto.sourceUrl());
                continue;
            }

            Article article = createArticleFromDto(dto);
            filtered.add(article);
        }

        if (!filtered.isEmpty()) {
            articleRepository.saveAll(filtered);
            log.info("기사 배치 저장 완료: {}개", filtered.size());
            filtered.forEach(article -> log.debug("저장된 기사: {}", article.getTitle()));
        }
    }
}
