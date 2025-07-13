package com.sprint.mission.sb03monewteam1.service;

import com.sprint.mission.sb03monewteam1.dto.CommentDto;
import com.sprint.mission.sb03monewteam1.dto.request.CommentRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponseCommentDto;
import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.entity.Comment;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import com.sprint.mission.sb03monewteam1.exception.article.InvalidCursorException;
import com.sprint.mission.sb03monewteam1.exception.comment.CommentException;
import com.sprint.mission.sb03monewteam1.exception.comment.InvalidSortOptionException;
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

        return commentMapper.toDto(savedComment);
    }

    @Override
    public CursorPageResponseCommentDto getCommentsWithCursorBySort(UUID articleId, String cursor,
        Instant nextAfter, int size, String sortBy, String sortDirection) {

        log.info(
            "댓글 목록 조회 시작: 기사 ID = {}, cursor = {}, nextAfter = {},  size = {}, sortBy = {}, direction = {}"
            , articleId, cursor, nextAfter, size, sortBy, sortDirection);

        Article article = articleRepository.findById(articleId)
            .orElseThrow(() -> new CommentException(ErrorCode.ARTICLE_NOT_FOUND));

        // 페이지 크기 유효성 검사
        if (size < 1) {
            throw new IllegalArgumentException("페이지 크기는 1 이상이어야 합니다");
        }

        String orderField = sortBy != null ? sortBy : "createdAt";
        String orderDirection = sortDirection != null ? sortDirection : "DESC";

        // 정렬 기준 유효성 검사
        if (!orderField.equals("createdAt") && !orderField.equals("likeCount")) {
            throw new InvalidSortOptionException("허용되지 않은 정렬 기준입니다");
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

        String nextCursor = null;
        Instant nextCursorAfter = null;

        if (hasNext) {
            Comment lastComment = comments.get(size - 1);
            // 동점 처리(Tie-breaking) : 스마트 커서 (id값 추가)
            if ("createdAt".equals(orderField)) {
                if (cursor != null) {
                    try {
                        Instant.parse(cursor);
                    } catch (DateTimeParseException e) {
                        throw new InvalidCursorException("유효하지 않은 커서입니다.");
                    }
                }
                nextCursor = lastComment.getCreatedAt().toString() + "_" + lastComment.getId().toString();
            } else {
                if (cursor != null) {
                    try {
                        Long.parseLong(cursor);
                    } catch (NumberFormatException e) {
                        throw new InvalidCursorException("유효하지 않은 커서입니다.");
                    }
                }
                nextCursor = lastComment.getLikeCount() + "_" + lastComment.getId().toString();
            }
            nextCursorAfter = lastComment.getCreatedAt();
        }

        Long totalElements = commentRepository.countByArticleId(articleId);
        comments = hasNext ? comments.subList(0, size) : comments;

        List<CommentDto> commentDtos = comments.stream()
            .map(commentMapper::toDto)
            .toList();

        if (nextCursor != null) {
            nextCursor = nextCursor.split("_")[0];
        }

        log.info("댓글 목록 조회 완료 - 조회된 댓글 수: {}, hasNext: {}", commentDtos.size(), hasNext);

        return new CursorPageResponseCommentDto(
            commentDtos,
            nextCursor,
            nextCursorAfter,
            size,
            totalElements,
            hasNext
        );
    }
}
