package com.sprint.mission.sb03monewteam1.service;

import com.sprint.mission.sb03monewteam1.dto.CommentActivityDto;
import com.sprint.mission.sb03monewteam1.dto.CommentDto;
import com.sprint.mission.sb03monewteam1.dto.CommentLikeActivityDto;
import com.sprint.mission.sb03monewteam1.dto.CommentLikeDto;
import com.sprint.mission.sb03monewteam1.dto.request.CommentCursorRequest;
import com.sprint.mission.sb03monewteam1.dto.request.CommentRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.request.CommentUpdateRequest;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponse;
import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.entity.Comment;
import com.sprint.mission.sb03monewteam1.entity.CommentLike;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.event.CommentActivityCreateEvent;
import com.sprint.mission.sb03monewteam1.event.CommentActivityDeleteEvent;
import com.sprint.mission.sb03monewteam1.event.CommentActivityUpdateEvent;
import com.sprint.mission.sb03monewteam1.event.CommentLikeActivityCreateEvent;
import com.sprint.mission.sb03monewteam1.event.CommentLikeActivityDeleteEvent;
import com.sprint.mission.sb03monewteam1.event.CommentLikeCountUpdateEvent;
import com.sprint.mission.sb03monewteam1.event.CommentLikeEvent;
import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import com.sprint.mission.sb03monewteam1.exception.comment.CommentAlreadyLikedException;
import com.sprint.mission.sb03monewteam1.exception.comment.CommentException;
import com.sprint.mission.sb03monewteam1.exception.comment.CommentLikeNotFoundException;
import com.sprint.mission.sb03monewteam1.exception.comment.CommentNotFoundException;
import com.sprint.mission.sb03monewteam1.exception.comment.UnauthorizedCommentAccessException;
import com.sprint.mission.sb03monewteam1.exception.common.InvalidCursorException;
import com.sprint.mission.sb03monewteam1.exception.common.InvalidSortOptionException;
import com.sprint.mission.sb03monewteam1.exception.user.UserNotFoundException;
import com.sprint.mission.sb03monewteam1.mapper.CommentActivityMapper;
import com.sprint.mission.sb03monewteam1.mapper.CommentLikeActivityMapper;
import com.sprint.mission.sb03monewteam1.mapper.CommentLikeMapper;
import com.sprint.mission.sb03monewteam1.mapper.CommentMapper;
import com.sprint.mission.sb03monewteam1.repository.jpa.article.ArticleRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.comment.CommentRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.commentLike.CommentLikeRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.user.UserRepository;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    private static final String ORDER_BY_CREATED_AT = "createdAt";
    private static final String ORDER_BY_LIKE_COUNT = "likeCount";
    private static final String DIRECTION_DESC = "DESC";
    private static final String DIRECTION_ASC = "ASC";
    private static final int MIN_PAGE_SIZE = 1;

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final CommentMapper commentMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final CommentActivityMapper commentActivityMapper;
    private final CommentLikeActivityMapper commentLikeActivityMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public CommentDto create(CommentRegisterRequest commentRegisterRequest) {

        UUID articleId = commentRegisterRequest.articleId();
        UUID userId = commentRegisterRequest.userId();
        String content = commentRegisterRequest.content();

        log.info("댓글 등록 시작: 기사 = {}, 작성자 = {}, 내용 = {}", articleId, userId, content);

        User user = userRepository.findByIdAndIsDeletedFalse(userId)
            .orElseThrow(() -> new CommentException(ErrorCode.USER_NOT_FOUND));
        Article article = articleRepository.findByIdAndIsDeletedFalse(articleId)
            .orElseThrow(() -> new CommentException(ErrorCode.ARTICLE_NOT_FOUND));

        Comment comment = Comment.builder()
            .content(content)
            .article(article)
            .author(user)
            .build();

        Comment savedComment = commentRepository.save(comment);
        article.increaseCommentCount();

        CommentActivityDto event = commentActivityMapper.toDto(savedComment);
        eventPublisher.publishEvent(new CommentActivityCreateEvent(userId, event));
        log.debug("댓글 작성 활동 내역 이벤트 발행 완료: {}", event);

        return commentMapper.toDto(savedComment).toBuilder()
            .likedByMe(false)
            .build();
    }

    @Override
    public CursorPageResponse<CommentDto> getCommentsWithCursorBySort(
        CommentCursorRequest request,
        UUID userId
    ) {
        UUID articleId = request.articleId();
        String cursor = request.cursor();
        Instant after = request.after();
        int limit = request.limit();
        String orderBy = (request.orderBy() != null) ? request.orderBy() : ORDER_BY_CREATED_AT;
        String direction = (request.direction() != null) ? request.direction() : DIRECTION_DESC;

        log.info("댓글 목록 조회 시작: 기사 ID = {}, cursor = {}, after = {}, size = {}, order = {} {}", articleId, cursor, after, limit, orderBy, direction);

        validateCursorRequest(articleId, limit, orderBy, direction, cursor);

        List<Comment> comments = commentRepository.findCommentsWithCursorBySort(
            articleId,
            cursor,
            after,
            limit + 1,
            orderBy,
            direction
        );

        boolean hasNext = comments.size() > limit;
        Comment lastComment = hasNext ? comments.get(comments.size() - 1) : null;
        comments = hasNext ? comments.subList(0, limit) : comments;

        String nextCursor = resolveNextCursor(lastComment, orderBy);
        Instant nextCursorAfter = lastComment != null ? lastComment.getCreatedAt() : null;

        Long totalElements = (articleId == null)
            ? commentRepository.countByIsDeletedFalse()
            : commentRepository.countByArticleIdAndIsDeletedFalse(articleId);

        List<CommentDto> commentDtos = convertToDtosWithLikedByMe(comments, userId);

        log.info("댓글 목록 조회 완료 - 조회된 댓글 수: {}, hasNext: {}", commentDtos.size(), hasNext);

        return new CursorPageResponse<CommentDto>(
            commentDtos,
            nextCursor,
            nextCursorAfter,
            limit,
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

        Comment comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
            .orElseThrow(() -> new CommentNotFoundException(commentId));

        validateAuthor(comment, userId);

        String newContent = commentUpdateRequest.content();
        comment.updateContent(newContent);

        log.info("댓글 수정 완료 : 댓글 ID = {}, 유저 ID = {}", commentId, userId);

        CommentActivityDto newContentActivity = commentActivityMapper.toDto(comment);
        CommentActivityUpdateEvent event = new CommentActivityUpdateEvent(userId, commentId,
            newContentActivity);
        eventPublisher.publishEvent(event);

        return toCommentDtoWithLikedByMe(comment, userId);
    }

    @Override
    public Comment delete(UUID commentId, UUID userId) {

        log.info("댓글 논리 삭제 시작 : 댓글 ID = {}, 유저 ID = {}", commentId, userId);

        Comment comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
            .orElseThrow(() -> new CommentNotFoundException(commentId));

        validateAuthor(comment, userId);

        comment.delete();
        comment.getArticle().decreaseCommentCount();

        log.info("댓글 논리 삭제 완료 : 댓글 ID = {}, 유저 ID = {}", commentId, userId);

        CommentActivityDeleteEvent event = new CommentActivityDeleteEvent(userId, commentId);
        eventPublisher.publishEvent(event);

        log.debug("댓글 작성 활동 내역 삭제 이벤트 발행 완료: {}", event);

        return comment;
    }

    @Override
    public void deleteHard(UUID commentId) {

        log.info("댓글 물리 삭제 시작 : 댓글 ID = {}", commentId);

        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new CommentNotFoundException(commentId));

        // 논리 삭제 되지 않은 댓글이면 기사의 댓글수 감소
        if (!comment.getIsDeleted()) {
            comment.getArticle().decreaseCommentCount();
        }

        commentLikeRepository.deleteByCommentId(commentId);
        commentRepository.deleteById(commentId);

        log.info("댓글 물리 삭제 완료 : 댓글 ID = {}", commentId);
    }

    @Override
    public CommentLikeDto like(UUID commentId, UUID userId) {

        log.info("댓글 좋아요 등록 시작 : 댓글 ID = {}, 유저 ID = {}", commentId, userId);

        // 댓글 유효성
        Comment comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
            .orElseThrow(() -> new CommentNotFoundException(commentId));

        // 유저 유효성
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        // 중복 댓글 확인
        if (commentLikeRepository.existsByCommentIdAndUserId(commentId, userId)) {
            throw new CommentAlreadyLikedException(commentId, userId);
        }

        // 좋아요 생성 후 저장
        CommentLike commentLike = CommentLike.builder()
            .comment(comment)
            .user(user)
            .build();
        commentLikeRepository.save(commentLike);

        // 댓글의 좋아요수 +1
        comment.increaseLikeCount();

        // 이벤트 발행
        eventPublisher.publishEvent(new CommentLikeEvent(user, comment));
        log.info("좋아요 등록 알림 이벤트 발행: likedBy={}, comment={}", user.getId(), comment.getId());

        CommentLikeCountUpdateEvent countEvent =
            new CommentLikeCountUpdateEvent(commentId, comment.getLikeCount());
        eventPublisher.publishEvent(countEvent);
        log.debug("댓글 좋아요 사용 기록 동기화 이벤트 발행 완료: {}", countEvent);

        log.info("댓글 좋아요 등록 완료 : 댓글 좋아요 ID = {}", commentLike.getId());

        CommentLikeActivityDto event = commentLikeActivityMapper.toDto(commentLike);
        eventPublisher.publishEvent(new CommentLikeActivityCreateEvent(userId, event));
        log.debug("댓글 좋아요 사용 기록 이벤트 발행 완료: {}", event);

        return commentLikeMapper.toDto(commentLike);
    }

    @Override
    public void likeCancel(UUID commentId, UUID userId) {

        log.info("댓글 좋아요 취소 시작 : 댓글 ID = {}, 유저 ID = {}", commentId, userId);

        // 댓글 유효성
        Comment comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
            .orElseThrow(() -> new CommentNotFoundException(commentId));

        // 댓글 좋아요 여부 확인
        CommentLike commentLike = commentLikeRepository.findByUserIdAndCommentId(userId, commentId)
            .orElseThrow(() -> new CommentLikeNotFoundException(userId, commentId));

        // 댓글 좋아요 -1
        commentLikeRepository.deleteById(commentLike.getId());
        comment.decreaseLikeCount();

        log.info("댓글 좋아요 취소 완료 : 댓글 ID = {}, 유저 ID = {}", commentId, userId);

        CommentLikeActivityDeleteEvent event = new CommentLikeActivityDeleteEvent(userId,
            comment.getId());
        eventPublisher.publishEvent(event);
        log.debug("댓글 좋아요 활동 삭제 이벤트 발행 완료: {}", event);
    }

    private void validateCursorRequest(UUID articleId, int limit, String orderBy, String direction, String cursor) {

        if (articleId != null) {
            articleRepository.findByIdAndIsDeletedFalse(articleId)
                .orElseThrow(() -> new CommentException(ErrorCode.ARTICLE_NOT_FOUND));
        }

        if (limit < MIN_PAGE_SIZE) {
            throw new IllegalArgumentException("페이지 크기는 1 이상이어야 합니다");
        }

        if (!orderBy.equals(ORDER_BY_CREATED_AT) && !orderBy.equals(ORDER_BY_LIKE_COUNT)) {
            throw new InvalidSortOptionException(ErrorCode.INVALID_SORT_FIELD, "orderBy",
                orderBy);
        }

        if (!direction.equals(DIRECTION_DESC) && !direction.equals(DIRECTION_ASC)) {
            throw new InvalidSortOptionException(ErrorCode.INVALID_SORT_DIRECTION, "direction",
                direction);
        }

        if (cursor != null) {
            switch (orderBy) {
                case ORDER_BY_CREATED_AT -> parseInstant(cursor);
                case ORDER_BY_LIKE_COUNT -> parseLong(cursor);
            }
        }
    }

    private String resolveNextCursor(Comment lastComment, String orderBy) {
        if (lastComment == null) return null;
        return switch (orderBy) {
            case ORDER_BY_CREATED_AT -> lastComment.getCreatedAt().toString();
            case ORDER_BY_LIKE_COUNT -> String.valueOf(lastComment.getLikeCount());
            default -> throw new InvalidSortOptionException(ErrorCode.INVALID_SORT_FIELD, "orderBy", orderBy);
        };
    }

    private void validateAuthor(Comment comment, UUID userId) {
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new UnauthorizedCommentAccessException();
        }
    }

    private List<CommentDto> convertToDtosWithLikedByMe(List<Comment> comments, UUID userId) {

        if (comments.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> commentIds = comments.stream()
            .map(Comment::getId)
            .toList();

        Set<UUID> likedCommentIds = (userId != null)
            ? commentLikeRepository.findLikedCommentIdsByUserIdAndCommentIds(userId, commentIds)
            : Collections.emptySet();

        return comments.stream()
            .map(comment -> {
                boolean likedByMe = likedCommentIds.contains(comment.getId());
                return commentMapper.toDto(comment)
                    .toBuilder()
                    .likedByMe(likedByMe)
                    .build();
            })
            .toList();
    }

    public CommentDto toCommentDtoWithLikedByMe(Comment comment, UUID userId) {
        boolean likedByMe = false;

        if (userId != null) {
            likedByMe = commentLikeRepository.existsByCommentIdAndUserId(comment.getId(), userId);
        }

        CommentDto dto = commentMapper.toDto(comment);
        if (dto == null) {
            throw new CommentException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        return dto.toBuilder().likedByMe(likedByMe).build();
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
