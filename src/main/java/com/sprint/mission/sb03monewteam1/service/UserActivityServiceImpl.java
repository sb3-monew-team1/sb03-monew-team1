package com.sprint.mission.sb03monewteam1.service;

import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.exception.user.UserException;
import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import com.sprint.mission.sb03monewteam1.document.*;
import com.sprint.mission.sb03monewteam1.dto.*;
import com.sprint.mission.sb03monewteam1.repository.jpa.UserRepository;
import com.sprint.mission.sb03monewteam1.repository.mongodb.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserActivityServiceImpl implements UserActivityService {

    private final UserRepository userRepository;
    private final SubscriptionActivityRepository subscriptionActivityRepository;
    private final CommentActivityRepository commentActivityRepository;
    private final CommentLikeActivityRepository commentLikeActivityRepository;
    private final ArticleViewActivityRepository articleViewActivityRepository;

    public UserActivityDto getUserActivity(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        List<SubscriptionActivityDto> subscriptions = subscriptionActivityRepository.findById(userId)
            .map(SubscriptionActivity::getSubscriptions)
            .orElse(List.of());

        List<CommentActivityDto> comments = commentActivityRepository
            .findRecent10CommentsByUserId(userId)
            .map(CommentActivity::getComments)
            .orElse(List.of());

        List<CommentLikeActivityDto> commentLikes = commentLikeActivityRepository
            .findRecent10CommentLikesByUserId(userId)
            .map(CommentLikeActivity::getCommentLikes)
            .orElse(List.of());

        List<ArticleViewActivityDto> articleViews = articleViewActivityRepository
            .findRecent10ArticleViewsByUserId(userId)
            .map(ArticleViewActivity::getArticleViews)
            .orElse(List.of());

        return UserActivityDto.builder()
            .id(user.getId())
            .email(user.getEmail())
            .nickname(user.getNickname())
            .createdAt(user.getCreatedAt())
            .subscriptions(subscriptions)
            .comments(comments)
            .commentLikes(commentLikes)
            .articleViews(articleViews)
            .build();
    }
}
