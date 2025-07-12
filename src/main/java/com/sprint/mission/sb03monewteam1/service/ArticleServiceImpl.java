package com.sprint.mission.sb03monewteam1.service;

import java.time.Instant;
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
        log.info("기사 뷰 등록 시작 - userId: {}, articleId: {}", userId, articleId);

        Article article = articleRepository.findByIdAndIsDeletedFalse(articleId)
                .orElseThrow(ArticleNotFoundException::new);

        if (articleViewRepository.existsByUserIdAndArticleId(userId, articleId)) {
            throw new DuplicateArticleViewException();
        }

        article.increaseViewCount();
        articleRepository.save(article);

        ArticleView articleView = ArticleView.createArticleView(userId, article);
        ArticleView savedArticleView = articleViewRepository.save(articleView);

        ArticleViewDto result = articleViewMapper.toDto(savedArticleView);
        log.info("기사 뷰 등록 완료 - id: {}", result.id());

        return result;
    }

    @Override
    public CursorPageResponseArticleDto getArticles(
            String searchKeyword,
            String source,
            List<String> interests,
            Instant startDate,
            Instant endDate,
            String sortBy,
            String cursor,
            int limit) {

        log.info("기사 목록 조회 시작 - searchKeyword: {}, source: {}, sortBy: {}, cursor: {}, limit: {}",
                searchKeyword, source, sortBy, cursor, limit);

        List<Article> articles;

        // 정렬 기준에 따라 다른 쿼리 호출
        if ("viewCount".equals(sortBy)) {
            Long cursorValue = cursor != null ? Long.valueOf(cursor) : null;
            articles = articleRepository.findArticlesWithCursorByViewCount(
                    searchKeyword, source, startDate, endDate, cursorValue, limit + 1);
        } else {
            // 기본값: publishDate 정렬
            Instant cursorValue = cursor != null ? Instant.parse(cursor) : null;
            articles = articleRepository.findArticlesWithCursorByDate(
                    searchKeyword, source, startDate, endDate, cursorValue, limit + 1);
        }

        // 다음 페이지 존재 여부 확인
        boolean hasNext = articles.size() > limit;
        if (hasNext) {
            articles = articles.subList(0, limit);
        }

        // DTO 변환
        List<ArticleDto> articleDtos = articles.stream()
                .map(articleMapper::toDto)
                .collect(Collectors.toList());

        // 다음 커서 생성
        String nextCursor = null;
        if (hasNext && !articles.isEmpty()) {
            Article lastArticle = articles.get(articles.size() - 1);
            nextCursor = "viewCount".equals(sortBy)
                    ? lastArticle.getViewCount().toString()
                    : lastArticle.getPublishDate().toString();
        }

        CursorPageResponseArticleDto result = CursorPageResponseArticleDto.builder()
                .articles(articleDtos)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();

        log.info("기사 목록 조회 완료 - 조회된 기사 수: {}, hasNext: {}", articleDtos.size(), hasNext);
        return result;
    }

    @Override
    public List<String> getSources() {
        log.info("기사 출처 목록 조회 시작");
        List<String> sources = articleRepository.findDistinctSources();
        log.info("기사 출처 목록 조회 완료 - 출처 수: {}", sources.size());
        return sources;
    }
}
