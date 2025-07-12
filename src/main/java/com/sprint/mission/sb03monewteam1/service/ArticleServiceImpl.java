package com.sprint.mission.sb03monewteam1.service;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sprint.mission.sb03monewteam1.dto.ArticleDto;
import com.sprint.mission.sb03monewteam1.dto.ArticleViewDto;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponseArticleDto;
import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.entity.ArticleView;
import com.sprint.mission.sb03monewteam1.exception.article.ArticleNotFoundException;
import com.sprint.mission.sb03monewteam1.exception.article.DuplicateArticleViewException;
import com.sprint.mission.sb03monewteam1.exception.article.InvalidCursorException;
import com.sprint.mission.sb03monewteam1.mapper.ArticleMapper;
import com.sprint.mission.sb03monewteam1.mapper.ArticleViewMapper;
import com.sprint.mission.sb03monewteam1.repository.ArticleRepository;
import com.sprint.mission.sb03monewteam1.repository.ArticleViewRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleViewRepository articleViewRepository;
    private final ArticleMapper articleMapper;
    private final ArticleViewMapper articleViewMapper;

    @Override
    @Transactional
    public ArticleViewDto createArticleView(UUID userId, UUID articleId) {
        if (articleViewRepository.existsByUserIdAndArticleId(userId, articleId)) {
            throw new DuplicateArticleViewException();
        }

        long updated = articleRepository.incrementViewCount(articleId);
        if (updated == 0) {
            throw new ArticleNotFoundException("기사를 찾을 수 없습니다.");
        }

        Article article = articleRepository.findByIdAndIsDeletedFalse(articleId)
                .orElseThrow(() -> new ArticleNotFoundException("기사를 찾을 수 없습니다."));

        ArticleView articleView = ArticleView.createArticleView(userId, article);
        ArticleView savedArticleView = articleViewRepository.save(articleView);

        ArticleViewDto result = articleViewMapper.toDto(savedArticleView);
        log.info("기사 뷰 등록 완료 - id: {}", result.id());

        return result;
    }

    @Override
    public CursorPageResponseArticleDto getArticles(
            String keyword,
            List<String> sourceIn,
            List<String> interests,
            Instant publishDateFrom,
            Instant publishDateTo,
            String orderBy,
            String direction,
            String cursor,
            Instant after,
            int limit) {

        log.info(
                "기사 목록 조회 시작 - keyword: {}, sourceIn: {}, orderBy: {}, direction: {}, cursor: {}, limit: {}",
                keyword, sourceIn, orderBy, direction, cursor, limit);

        String sortBy = orderBy != null ? orderBy : "publishDate";
        boolean isAscending = "ASC".equalsIgnoreCase(direction);

        List<Article> articles = getArticlesBySortType(
                keyword, sourceIn, publishDateFrom, publishDateTo,
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

        CursorPageResponseArticleDto result = CursorPageResponseArticleDto.builder()
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
                        throw new InvalidCursorException("잘못된 조회수 커서 형식입니다: " + cursor);
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
                        throw new InvalidCursorException("잘못된 댓글수 커서 형식입니다: " + cursor);
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
                        throw new InvalidCursorException("잘못된 날짜 커서 형식입니다: " + cursor);
                    }
                }
                return articleRepository.findArticlesWithCursorByDate(
                        keyword, sourceIn, publishDateFrom, publishDateTo, dateCursor, limit + 1, isAscending);
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
}
