package com.sprint.mission.sb03monewteam1.event.listener;

import com.sprint.mission.sb03monewteam1.document.CommentLikeActivity;
import com.sprint.mission.sb03monewteam1.dto.CommentLikeActivityDto;
import com.sprint.mission.sb03monewteam1.event.CommentLikeActivityCreateEvent;
import com.sprint.mission.sb03monewteam1.event.CommentLikeActivityDeleteEvent;
import com.sprint.mission.sb03monewteam1.repository.mongodb.CommentLikeActivityRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentLikeActivityEventListener extends AbstractActivityEventListener<
    CommentLikeActivityDto,
    CommentLikeActivity,
    CommentLikeActivityRepository> {

    private final CommentLikeActivityRepository repository;

    @Override
    protected UUID getActivityId(CommentLikeActivityDto dto) {
        return dto.commentId();
    }

    @Override
    protected CommentLikeActivityRepository getRepository() {
        return repository;
    }

    @Override
    protected List<CommentLikeActivityDto> getActivityList(CommentLikeActivity document) {
        return document.getCommentLikes();
    }

    @Override
    protected CommentLikeActivity createNewDocument(UUID userId) {
        CommentLikeActivity activity = new CommentLikeActivity();
        activity.setUserId(userId);
        activity.setCommentLikes(new ArrayList<>());
        return activity;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCreateEvent(CommentLikeActivityCreateEvent event) {
        log.debug("CommentLikeActivityCreateEvent 리스너 실행: {}", event);

        saveUserActivity(event.userId(), event.commentLikeActivityDto());
    }


    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDeleteEvent(CommentLikeActivityDeleteEvent event) {
        log.debug("CommentLikeActivityDeleteEvent 리스너 실행: {}", event);
        deleteUserActivity(event.userId(), event.commentId());
    }
}
