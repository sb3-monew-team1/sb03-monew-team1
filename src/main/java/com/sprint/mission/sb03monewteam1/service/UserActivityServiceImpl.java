package com.sprint.mission.sb03monewteam1.service;

import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.exception.user.UserException;
import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import com.sprint.mission.sb03monewteam1.document.*;
import com.sprint.mission.sb03monewteam1.dto.*;
import com.sprint.mission.sb03monewteam1.repository.jpa.user.UserRepository;
import com.sprint.mission.sb03monewteam1.repository.mongodb.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActivityServiceImpl implements UserActivityService {

    private final UserRepository userRepository;
    private final SubscriptionActivityRepository subscriptionActivityRepository;
    private final CommentActivityRepository commentActivityRepository;
    private final CommentLikeActivityRepository commentLikeActivityRepository;
    private final ArticleViewActivityRepository articleViewActivityRepository;

    public UserActivityDto getUserActivity(UUID userId) {
        log.info("사용자 활동 조회 요청: userId={}", userId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        log.debug("사용자 조회 성공: userId={}, email={}", user.getId(), user.getEmail());

        List<SubscriptionDto> subscriptions = subscriptionActivityRepository.findById(userId)
            .map(activity -> {
                log.debug("구독 활동 조회 성공: 수={}", activity.getSubscriptions().size());
                return activity.getSubscriptions();
            })
            .orElseGet(() -> {
                log.debug("구독 활동 없음: userId={}", userId);
                return List.of();
            });

        List<CommentActivityDto> comments = commentActivityRepository
            .findRecent10CommentsByUserId(userId)
            .map(activity -> {
                log.debug("댓글 활동 조회 성공: 수={}", activity.getComments().size());
                return activity.getComments();
            })
            .orElseGet(() -> {
                log.debug("댓글 활동 없음: userId={}", userId);
                return List.of();
            });

        List<CommentLikeActivityDto> commentLikes = commentLikeActivityRepository
            .findRecent10CommentLikesByUserId(userId)
            .map(activity -> {
                log.debug("댓글 좋아요 활동 조회 성공: 수={}", activity.getCommentLikes().size());
                return activity.getCommentLikes();
            })
            .orElseGet(() -> {
                log.debug("댓글 좋아요 활동 없음: userId={}", userId);
                return List.of();
            });

        List<ArticleViewActivityDto> articleViews = articleViewActivityRepository
            .findRecent10ArticleViewsByUserId(userId)
            .map(activity -> {
                log.debug("기사 조회 활동 조회 성공: 수={}", activity.getArticleViews().size());
                return activity.getArticleViews();
            })
            .orElseGet(() -> {
                log.debug("기사 조회 활동 없음: userId={}", userId);
                return List.of();
            });

        UserActivityDto result = UserActivityDto.builder()
            .id(user.getId())
            .email(user.getEmail())
            .nickname(user.getNickname())
            .createdAt(user.getCreatedAt())
            .subscriptions(subscriptions)
            .comments(comments)
            .commentLikes(commentLikes)
            .articleViews(articleViews)
            .build();

        log.info("사용자 활동 응답 생성 완료: userId={}", userId);
        return result;
    }
}