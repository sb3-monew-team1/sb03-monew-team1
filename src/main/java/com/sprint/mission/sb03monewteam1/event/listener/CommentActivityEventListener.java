package com.sprint.mission.sb03monewteam1.event.listener;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import com.sprint.mission.sb03monewteam1.document.CommentActivity;
import com.sprint.mission.sb03monewteam1.dto.CommentActivityDto;
import com.sprint.mission.sb03monewteam1.event.CommentActivityCreateEvent;
import com.sprint.mission.sb03monewteam1.repository.mongodb.CommentActivityRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentActivityEventListener extends AbstractActivityEventListener<
    CommentActivityDto,
    CommentActivity,
    CommentActivityRepository> {

    private final CommentActivityRepository repository;

    @Override
    protected UUID getUserId(CommentActivityDto dto) {
        return dto.userId();
    }

    @Override
    protected UUID getActivityId(CommentActivityDto dto) {
        return dto.id();
    }

    @Override
    protected CommentActivityRepository getRepository() {
        return repository;
    }

    @Override
    protected List<CommentActivityDto> getActivityList(CommentActivity document) {
        return document.getComments();
    }

    @Override
    protected CommentActivity createNewDocument(UUID userId) {
        CommentActivity activity = new CommentActivity();
        activity.setUserId(userId);
        activity.setComments(new ArrayList<>());
        return activity;

    }

    @Async
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void handle(CommentActivityCreateEvent event) {
        log.debug("리스너 실행: {}", event);
        saveUserActivity(event.commentActivityDto());
    }
}

