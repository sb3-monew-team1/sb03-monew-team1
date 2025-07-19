package com.sprint.mission.sb03monewteam1.service;

import com.sprint.mission.sb03monewteam1.dto.CommentDto;
import com.sprint.mission.sb03monewteam1.dto.CommentLikeActivityDto;
import com.sprint.mission.sb03monewteam1.dto.CommentLikeDto;
import com.sprint.mission.sb03monewteam1.dto.CommentActivityDto;
import com.sprint.mission.sb03monewteam1.dto.request.CommentRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.request.CommentUpdateRequest;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponse;
import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.entity.Comment;
import com.sprint.mission.sb03monewteam1.entity.CommentLike;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.event.CommentActivityCreateEvent;
import com.sprint.mission.sb03monewteam1.event.CommentLikeActivityCreateEvent;
import com.sprint.mission.sb03monewteam1.event.CommentLikeActivityDeleteEvent;
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
import com.sprint.mission.sb03monewteam1.repository.jpa.ArticleRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.CommentLikeRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.CommentRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.UserRepository;
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
        eventPublisher.publishEvent(new CommentActivityCreateEvent(event));
        log.debug("댓글 작성 활동 내역 이벤트 발행 완료: {}", event);

        return commentMapper.toDto(savedComment);
    }

    @Override
    public CursorPageResponse<CommentDto> getCommentsWithCursorBySort(
        UUID articleId, String cursor,
        Instant nextAfter, int size,
        String sortBy, String sortDirection, UUID userId) {

        log.info(
            "댓글 목록 조회 시작: 기사 ID = {}, cursor = {}, nextAfter = {},  size = {}, sortBy = {}, direction = {}"
            , articleId, cursor, nextAfter, size, sortBy, sortDirection);

        if (articleId != null) {
            articleRepository.findByIdAndIsDeletedFalse(articleId)
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

        List<UUID> commentIds = comments.stream()
            .map(Comment::getId)
            .toList();

        Set<UUID> likedCommentIds = (userId != null && !commentIds.isEmpty())
            ? commentLikeRepository.findLikedCommentIdsByUserIdAndCommentIds(userId, commentIds)
            : Collections.emptySet();

        List<CommentDto> commentDtos = comments.stream()
            .map(comment -> {
                boolean likedByMe = likedCommentIds.contains(comment.getId());
                return commentMapper.toDto(comment)
                    .toBuilder()
                    .likedByMe(likedByMe)
                    .build();
            })
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

        Comment comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
            .orElseThrow(() -> new CommentNotFoundException(commentId));

        validateAuthor(comment, userId);

        String newContent = commentUpdateRequest.content();
        comment.updateContent(newContent);

        log.info("댓글 수정 완료 : 댓글 ID = {}, 유저 ID = {}", commentId, userId);

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

        log.info("댓글 좋아요 등록 완료 : 댓글 좋아요 ID = {}", commentLike.getId());

        CommentLikeActivityDto event = commentLikeActivityMapper.toDto(commentLike);
        eventPublisher.publishEvent(new CommentLikeActivityCreateEvent(event));
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

        CommentLikeActivityDeleteEvent event = new CommentLikeActivityDeleteEvent(userId, commentId);
        eventPublisher.publishEvent(event);
        log.debug("댓글 좋아요 활동 삭제 이벤트 발행 완료: {}", event);
    }

    private void validateAuthor(Comment comment, UUID userId) {
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new UnauthorizedCommentAccessException();
        }
    }

    public CommentDto toCommentDtoWithLikedByMe(Comment comment, UUID userId) {
        boolean likedByMe = false;

        if (userId != null) {
            likedByMe = commentLikeRepository.existsByCommentIdAndUserId(comment.getId(), userId);
        }

        CommentDto dto = commentMapper.toDto(comment);
        if (dto == null) throw new CommentException(ErrorCode.INTERNAL_SERVER_ERROR);
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
