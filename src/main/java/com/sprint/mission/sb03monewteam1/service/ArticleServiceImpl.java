package com.sprint.mission.sb03monewteam1.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.sb03monewteam1.collector.HankyungNewsCollector;
import com.sprint.mission.sb03monewteam1.collector.NaverNewsCollector;
import com.sprint.mission.sb03monewteam1.config.metric.MonewMetrics;
import com.sprint.mission.sb03monewteam1.dto.ArticleDto;
import com.sprint.mission.sb03monewteam1.dto.ArticleRestoreResultDto;
import com.sprint.mission.sb03monewteam1.dto.ArticleViewActivityDto;
import com.sprint.mission.sb03monewteam1.dto.ArticleViewDto;
import com.sprint.mission.sb03monewteam1.dto.CollectedArticleDto;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponse;
import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.entity.ArticleInterest;
import com.sprint.mission.sb03monewteam1.entity.ArticleView;
import com.sprint.mission.sb03monewteam1.entity.Comment;
import com.sprint.mission.sb03monewteam1.entity.InterestKeyword;
import com.sprint.mission.sb03monewteam1.event.ArticleViewActivityCreateEvent;
import com.sprint.mission.sb03monewteam1.event.NewArticleCollectEvent;
import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import com.sprint.mission.sb03monewteam1.exception.article.ArticleNotFoundException;
import com.sprint.mission.sb03monewteam1.exception.common.InvalidCursorException;
import com.sprint.mission.sb03monewteam1.mapper.ArticleMapper;
import com.sprint.mission.sb03monewteam1.mapper.ArticleViewActivityMapper;
import com.sprint.mission.sb03monewteam1.mapper.ArticleViewMapper;
import com.sprint.mission.sb03monewteam1.repository.jpa.article.ArticleRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.articleInterest.ArticleInterestRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.articleView.ArticleViewRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.comment.CommentRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.commentLike.CommentLikeRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.interest.InterestKeywordRepository;
import com.sprint.mission.sb03monewteam1.util.S3Util;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleViewRepository articleViewRepository;
    private final ArticleInterestRepository articleInterestRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final InterestKeywordRepository interestKeywordRepository;

    private final ArticleMapper articleMapper;
    private final ArticleViewMapper articleViewMapper;
    private final ArticleViewActivityMapper articleViewActivityMapper;

    private final NaverNewsCollector naverNewsCollector;
    private final HankyungNewsCollector hankyungNewsCollector;

    private final ApplicationEventPublisher eventPublisher;

    private final MonewMetrics monewMetrics;

    private final ObjectMapper objectMapper;
    private final S3Util s3Util;

    @Value("${aws.s3.bucket:}")
    private String backupBucket;

    @Value("${aws.s3.backup-prefix:articles/}")
    private String backupPrefix;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Override
    @Transactional
    public ArticleViewDto createArticleView(UUID userId, UUID articleId) {
        List<ArticleView> existingList = articleViewRepository.findByUserIdAndArticleId(userId,
            articleId);
        if (!existingList.isEmpty()) {
            return articleViewMapper.toDto(existingList.get(0));
        }

        long updated = articleRepository.incrementViewCount(articleId);
        if (updated == 0) {
            throw new ArticleNotFoundException(articleId.toString());
        }

        Article article = articleRepository.findByIdAndIsDeletedFalse(articleId)
            .orElseThrow(() -> new ArticleNotFoundException(articleId.toString()));

        ArticleView articleView = ArticleView.createArticleView(userId, article);
        ArticleView savedArticleView = articleViewRepository.save(articleView);

        ArticleViewActivityDto event = articleViewActivityMapper.toDto(savedArticleView);
        eventPublisher.publishEvent(new ArticleViewActivityCreateEvent(userId, event));
        log.debug("기사 뷰 활동 내역 이벤트 발행 완료: {}", event);

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
        int limit,
        UUID userId) {

        Instant from = parseToKstInstant(publishDateFrom);
        Instant to = parseToKstInstant(publishDateTo);

        String sortBy = (orderBy != null) ? orderBy : "publishDate";
        boolean isAscending = "ASC".equalsIgnoreCase(direction);

        List<Article> articles = getArticlesBySortType(
            keyword, sourceIn, from, to,
            sortBy, isAscending, cursor, after, limit
        );

        boolean hasNext = articles.size() > limit;
        if (hasNext) {
            articles = articles.subList(0, limit);
        }

        List<UUID> articleIds = articles.stream()
            .map(Article::getId)
            .toList();

        Set<UUID> viewedIds = (userId != null && !articleIds.isEmpty())
            ? articleViewRepository.findViewedArticleIdsByUserIdAndArticleIds(userId, articleIds)
            : java.util.Collections.emptySet();

        List<ArticleDto> articleDtos = articles.stream()
            .map(article -> articleMapper.toDto(article, viewedIds.contains(article.getId())))
            .toList();

        String nextCursor = generateNextCursor(articles, sortBy, hasNext);
        Instant nextAfter = generateNextAfter(articles, hasNext);

        return CursorPageResponse.<ArticleDto>builder()
            .content(articleDtos)
            .nextCursor(nextCursor)
            .nextAfter(nextAfter)
            .size(articleDtos.size())
            .totalElements(null)
            .hasNext(hasNext)
            .build();
    }

    private List<Article> getArticlesBySortType(
        String keyword, List<String> sourceIn, Instant publishDateFrom, Instant publishDateTo,
        String sortBy, boolean isAscending, String cursor, Instant after, int limit) {

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
                return articleRepository.findArticlesWithCursorByViewCount(
                    keyword, sourceIn, publishDateFrom, publishDateTo,
                    viewCountCursor, after, limit + 1, isAscending);

            case "commentCount":
                Long commentCountCursor = null;
                if (cursor != null) {
                    try {
                        commentCountCursor = Long.valueOf(cursor);
                    } catch (NumberFormatException e) {
                        throw new InvalidCursorException(ErrorCode.INVALID_CURSOR_COUNT, cursor);
                    }
                }
                return articleRepository.findArticlesWithCursorByCommentCount(
                    keyword, sourceIn, publishDateFrom, publishDateTo,
                    commentCountCursor, after, limit + 1, isAscending);

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
                return String.valueOf(lastArticle.getViewCount());
            case "commentCount":
                return String.valueOf(lastArticle.getCommentCount());
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
    @Transactional(readOnly = true)
    public List<Article> collectNaverArticles(String keyword) {
        log.debug("네이버 기사 수집(엔티티 변환) 시작: 키워드={}", keyword);
        List<CollectedArticleDto> collectedArticles = naverNewsCollector.collect(keyword);

        List<String> allSourceUrls = collectedArticles.stream()
            .map(CollectedArticleDto::sourceUrl)
            .toList();

        List<String> existingUrls = articleRepository.findAllBySourceUrlIn(allSourceUrls);

        return collectedArticles.stream()
            .filter(dto -> shouldIncludeArticle(dto, keyword))
            .filter(dto -> !existingUrls.contains(dto.sourceUrl()))
            .map(articleMapper::toEntity)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Article> collectHankyungArticles(String keyword) {
        log.debug("한국경제 기사 수집(엔티티 변환) 시작: 키워드={}", keyword);
        List<CollectedArticleDto> collectedArticles = hankyungNewsCollector.collect(keyword);

        List<String> allSourceUrls = collectedArticles.stream()
            .map(CollectedArticleDto::sourceUrl)
            .toList();

        List<String> existingUrls = articleRepository.findAllBySourceUrlIn(allSourceUrls);

        return collectedArticles.stream()
            .filter(dto -> shouldIncludeArticle(dto, keyword))
            .filter(dto -> !existingUrls.contains(dto.sourceUrl()))
            .map(articleMapper::toEntity)
            .toList();
    }

    @Override
    @Transactional
    public void delete(UUID articleId) {
        Article article = getActiveArticle(articleId);

        article.markAsDeleted();
    }

    @Override
    @Transactional
    public void deleteHard(UUID articleId) {
        articleRepository.findById(articleId)
            .orElseThrow(() -> new ArticleNotFoundException(articleId.toString()));

        List<Comment> comments = commentRepository.findAllByArticleId(articleId);

        for (Comment comment : comments) {
            UUID commentId = comment.getId();
            commentLikeRepository.deleteByCommentId(commentId);
            commentRepository.deleteById(commentId);
        }

        articleViewRepository.deleteByArticleId(articleId);
        articleInterestRepository.deleteByArticleId(articleId);

        articleRepository.deleteById(articleId);
    }

    private Article getActiveArticle(UUID articleId) {
        return articleRepository.findByIdAndIsDeletedFalse(articleId)
            .orElseThrow(() -> new ArticleNotFoundException(articleId.toString()));
    }

    private boolean shouldIncludeArticle(CollectedArticleDto dto, String keyword) {
        String kw = keyword.toLowerCase();
        return (dto.title() != null && dto.title().toLowerCase().contains(kw))
            || (dto.summary() != null && dto.summary().toLowerCase().contains(kw));
    }

    @Override
    @Transactional
    public void saveArticles(List<Article> articles, String keyword) {
        if (articles == null || articles.isEmpty()) {
            return;
        }

        // 수집 시 이미 필터링을 하지만 트랜잭션으로 인한 중복 저장을 방지합니다.
        List<String> sourceUrls = articles.stream()
            .map(Article::getSourceUrl)
            .toList();
        List<String> existingUrls = articleRepository.findAllBySourceUrlIn(sourceUrls);

        List<Article> filteredArticles = articles.stream()
            .filter(article -> !existingUrls.contains(article.getSourceUrl()))
            .toList();

        if (filteredArticles.isEmpty()) {
            log.info("저장할 신규 기사가 없습니다.");
            return;
        }

        articleRepository.saveAll(filteredArticles);
        monewMetrics.getArticleCreatedCounter().increment(filteredArticles.size());

        // 저장할 모든 (article_id, interest_id) 조합 생성
        List<InterestKeyword> interestKeywords = interestKeywordRepository.findAllByKeyword(
            keyword);
        List<UUID> articleIds = filteredArticles.stream()
            .map(Article::getId)
            .toList();
        List<UUID> interestIds = interestKeywords.stream()
            .map(ik -> ik.getInterest().getId())
            .distinct()
            .toList();

        // 이미 존재하는 (article_id, interest_id) 조합을 한 번에 조회
        Set<Pair<UUID, UUID>> existingPairs = articleInterestRepository
            .findExistingArticleInterestPairsAsPairs(articleIds, interestIds);

        // 저장할 ArticleInterest 리스트 생성
        List<ArticleInterest> articleInterestList = new java.util.ArrayList<>();
        for (UUID interestId : interestIds) {
            for (Article article : filteredArticles) {
                Pair<UUID, UUID> pair = Pair.of(article.getId(), interestId);
                if (!existingPairs.contains(pair)) {
                    articleInterestList.add(
                        ArticleInterest.builder()
                            .article(article)
                            .interest(interestKeywords.stream()
                                .filter(ik -> ik.getInterest().getId().equals(interestId))
                                .findFirst().get().getInterest())
                            .build()
                    );
                }
            }
        }

        if (!articleInterestList.isEmpty()) {
            articleInterestRepository.saveAll(articleInterestList);

            for (InterestKeyword ik : interestKeywords) {
                monewMetrics.getInterestArticleMappedCounter(ik.getInterest().getId(),
                        ik.getInterest().getName())
                    .increment(filteredArticles.size());
            }
        }

        for (UUID interestId : interestIds) {
            String interestName = interestKeywords.stream()
                .filter(ik -> ik.getInterest().getId().equals(interestId))
                .findFirst()
                .map(ik -> ik.getInterest().getName())
                .orElse("Unknown Interest");
            List<ArticleDto> articleDtos = filteredArticles.stream()
                .map(articleMapper::toDto)
                .toList();

            eventPublisher.publishEvent(
                new NewArticleCollectEvent(interestId, interestName, articleDtos)
            );
        }
    }

    @Override
    @Transactional
    public List<ArticleRestoreResultDto> restoreArticles(String from, String to) {
        LocalDate fromDate;
        LocalDate toDate;
        try {
            fromDate = Instant.parse(from).atZone(KST).toLocalDate();
            toDate = Instant.parse(to).atZone(KST).toLocalDate();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("잘못된 날짜 형식입니다: " + e.getMessage(), e);
        }

        List<ArticleRestoreResultDto> results = new ArrayList<>();
        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            results.add(restoreByDate(date));
        }
        return results;
    }

    private ArticleRestoreResultDto restoreByDate(LocalDate date) {
        String key = backupPrefix + "backup-articles-" + date + ".json";
        List<UUID> restoredIds = new ArrayList<>();
        final int BATCH_SIZE = 1000;
        try {
            byte[] data = s3Util.download(backupBucket, key);
            if (data == null || data.length == 0) {
                return buildResult(date, restoredIds);
            }

            List<ArticleDto> batch = new ArrayList<>(BATCH_SIZE);
            Set<String> allSourceUrls = new HashSet<>();
            ObjectMapper mapper = objectMapper;
            try (InputStream is = new ByteArrayInputStream(data)) {
                JsonParser parser = mapper.getFactory().createParser(is);
                if (parser.nextToken() != JsonToken.START_ARRAY) {
                    return buildResult(date, restoredIds);
                }

                while (parser.nextToken() == JsonToken.START_OBJECT) {
                    ArticleDto dto = mapper.readValue(parser, ArticleDto.class);
                    batch.add(dto);
                    allSourceUrls.add(dto.sourceUrl());

                    if (batch.size() == BATCH_SIZE) {
                        restoredIds.addAll(processBatch(batch, allSourceUrls));
                        batch.clear();
                        allSourceUrls.clear();
                    }
                }
                if (!batch.isEmpty()) {
                    restoredIds.addAll(processBatch(batch, allSourceUrls));
                }
            }
        } catch (Exception e) {
            log.error("Failed to restore articles for date {}", date, e);
        }
        return buildResult(date, restoredIds);
    }

    private List<UUID> processBatch(List<ArticleDto> batch, Set<String> allSourceUrls) {
        List<UUID> restoredIds = new ArrayList<>();
        List<String> existingUrls = articleRepository.findAllBySourceUrlIn(
            new ArrayList<>(allSourceUrls));
        Set<String> existingUrlSet = new HashSet<>(existingUrls);

        List<Article> newArticles = new ArrayList<>();
        for (ArticleDto dto : batch) {
            if (existingUrlSet.contains(dto.sourceUrl())) {
                continue;
            }

            Article article = Article.builder()
                .id(dto.id())
                .source(dto.source())
                .sourceUrl(dto.sourceUrl())
                .title(dto.title())
                .publishDate(dto.publishDate())
                .summary(dto.summary())
                .viewCount(dto.viewCount())
                .commentCount(dto.commentCount())
                .isDeleted(false)
                .build();
            newArticles.add(article);
            restoredIds.add(dto.id());
        }
        if (!newArticles.isEmpty()) {
            articleRepository.saveAll(newArticles);
        }
        return restoredIds;
    }

    private ArticleRestoreResultDto buildResult(LocalDate date, List<UUID> ids) {
        return ArticleRestoreResultDto.builder()
            .restoreDate(date.atStartOfDay(KST).toInstant())
            .restoredArticleIds(ids)
            .restoredArticleCount(ids.size())
            .build();
    }
}
