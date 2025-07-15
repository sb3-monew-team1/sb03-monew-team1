package com.sprint.mission.sb03monewteam1.service;

import com.sprint.mission.sb03monewteam1.dto.CommentDto;
import com.sprint.mission.sb03monewteam1.dto.request.CommentRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.request.CommentUpdateRequest;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponse;
import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.entity.Comment;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import com.sprint.mission.sb03monewteam1.exception.comment.CommentException;
import com.sprint.mission.sb03monewteam1.exception.comment.CommentNotFoundException;
import com.sprint.mission.sb03monewteam1.exception.comment.UnauthorizedCommentAccessException;
import com.sprint.mission.sb03monewteam1.exception.common.InvalidCursorException;
import com.sprint.mission.sb03monewteam1.exception.common.InvalidSortOptionException;
import com.sprint.mission.sb03monewteam1.mapper.CommentMapper;
import com.sprint.mission.sb03monewteam1.repository.ArticleRepository;
import com.sprint.mission.sb03monewteam1.repository.CommentRepository;
import com.sprint.mission.sb03monewteam1.repository.UserRepository;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;
    private final CommentMapper commentMapper;

    @Override
    public CommentDto create(CommentRegisterRequest commentRegisterRequest) {

        UUID articleId = commentRegisterRequest.articleId();
        UUID userId = commentRegisterRequest.userId();
        String content = commentRegisterRequest.content();

        log.info("댓글 등록 시작: 기사 = {}, 작성자 = {}, 내용 = {}", articleId, userId, content);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CommentException(ErrorCode.USER_NOT_FOUND));
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new CommentException(ErrorCode.ARTICLE_NOT_FOUND));

        Comment comment = Comment.builder()
                .content(content)
                .article(article)
                .author(user)
                .build();

        Comment savedComment = commentRepository.save(comment);
        // 기사 댓글 수 증가
        article.increaseCommentCount();

        return commentMapper.toDto(savedComment);
    }

    @Override
    public CursorPageResponse<CommentDto> getCommentsWithCursorBySort(
        UUID articleId, String cursor,
        Instant nextAfter, int size,
        String sortBy, String sortDirection) {

        log.info(
            "댓글 목록 조회 시작: 기사 ID = {}, cursor = {}, nextAfter = {},  size = {}, sortBy = {}, direction = {}"
            , articleId, cursor, nextAfter, size, sortBy, sortDirection);

        if (articleId != null) {
            articleRepository.findById(articleId)
                .orElseThrow(() -> new CommentException(ErrorCode.ARTICLE_NOT_FOUND));
        }

        // 페이지 크기 유효성 검사
        if (size < 1) {
            throw new IllegalArgumentException("페이지 크기는 1 이상이어야 합니다");
        }

        String orderField = (sortBy != null) ? sortBy : "createdAt";
        String orderDirection = (sortDirection != null) ? sortDirection : "DESC";

        // 정렬 기준 유효성 검사
        if (!orderField.equals("createdAt") && !orderField.equals("likeCount")) {
            throw new InvalidSortOptionException(ErrorCode.INVALID_SORT_FIELD, "sortBy", orderField);
        }

        // 정렬 방향 유효성 검사
        if (!orderDirection.equals("DESC") && !orderDirection.equals("ASC")) {
            throw new InvalidSortOptionException(ErrorCode.INVALID_SORT_DIRECTION, "sortDirection", orderDirection);
        }

        // 커서 유효성 검사
        if (cursor != null) {
            switch (orderField) {
                case "createdAt" -> parseInstant(cursor);
                case "likeCount" -> parseLong(cursor);
                default -> throw new InvalidSortOptionException(ErrorCode.INVALID_SORT_FIELD, "sortBy", orderField);
            };
        }

        List<Comment> comments = commentRepository.findCommentsWithCursorBySort(
            articleId,
            cursor,
            nextAfter,
            size + 1,
            orderField,
            orderDirection
        );

        boolean hasNext = comments.size() > size;
        Comment lastComment = hasNext ? comments.get(comments.size() - 1) : null;

        String nextCursor = null;
        Instant nextCursorAfter = null;

        if (hasNext && lastComment != null) {
            nextCursor = switch (orderField) {
                case "createdAt" -> lastComment.getCreatedAt().toString();
                case "likeCount" -> String.valueOf(lastComment.getLikeCount());
                default -> throw new InvalidSortOptionException(ErrorCode.INVALID_SORT_FIELD, "sortBy", orderField);
            };
            nextCursorAfter = lastComment.getCreatedAt();
        }

        Long totalElements = (articleId == null)
            ? commentRepository.count()
            : commentRepository.countByArticleId(articleId);

        comments = hasNext ? comments.subList(0, size) : comments;
        List<CommentDto> commentDtos = comments.stream()
            .map(commentMapper::toDto)
            .toList();

        log.info("댓글 목록 조회 완료 - 조회된 댓글 수: {}, hasNext: {}", commentDtos.size(), hasNext);

        return new CursorPageResponse<CommentDto>(
            commentDtos,
            nextCursor,
            nextCursorAfter,
            size,
            totalElements,
            hasNext
        );
    }

    @Override
    public CommentDto update(
        UUID commentId,
        UUID userId,
        CommentUpdateRequest commentUpdateRequest) {

        log.info("댓글 수정 시작 : 댓글 ID = {}, 유저 ID = {}", commentId, userId);

        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new CommentNotFoundException(commentId));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new UnauthorizedCommentAccessException();
        }

        String newContent = commentUpdateRequest.content();

        comment.updateContent(newContent);

        log.info("댓글 수정 완료 : 댓글 ID = {}, 유저 ID = {}", commentId, userId);

        return commentMapper.toDto(comment);
    }

    @Override
    public Comment delete(UUID commentId) {
        return null;
    }

    private Instant parseInstant(String cursorValue) {
        try {
            return Instant.parse(cursorValue);
        } catch (DateTimeParseException e) {
            throw new InvalidCursorException(ErrorCode.INVALID_CURSOR_DATE, cursorValue);
        }
    }

    private Long parseLong(String cursorValue) {
        try {
            return Long.parseLong(cursorValue);
        } catch (NumberFormatException e) {
            throw new InvalidCursorException(ErrorCode.INVALID_CURSOR_COUNT, cursorValue);
        }
    }
}
