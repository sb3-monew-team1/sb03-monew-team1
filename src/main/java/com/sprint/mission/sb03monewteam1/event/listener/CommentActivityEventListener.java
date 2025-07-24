package com.sprint.mission.sb03monewteam1.event.listener;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import com.mongodb.client.result.UpdateResult;
import com.sprint.mission.sb03monewteam1.document.CommentActivity;
import com.sprint.mission.sb03monewteam1.dto.CommentActivityDto;
import com.sprint.mission.sb03monewteam1.event.CommentActivityCreateEvent;
import com.sprint.mission.sb03monewteam1.event.CommentActivityDeleteEvent;
import com.sprint.mission.sb03monewteam1.event.CommentActivityUpdateEvent;
import com.sprint.mission.sb03monewteam1.event.CommentLikeCountActivityUpdateEvent;
import com.sprint.mission.sb03monewteam1.event.UserNameActivityUpdateEvent;
import com.sprint.mission.sb03monewteam1.repository.mongodb.CommentActivityRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentActivityEventListener extends AbstractActivityEventListener<
    CommentActivityDto,
    CommentActivity,
    CommentActivityRepository> {

    private final MongoTemplate mongoTemplate;

    private final CommentActivityRepository repository;

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
    public void handleCreateEvent(CommentActivityCreateEvent event) {
        log.debug("CommentActivityCreateEvent 리스너 실행: {}", event);
        saveUserActivity(event.userId(), event.commentActivityDto());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDeleteEvent(CommentActivityDeleteEvent event) {
        log.debug("CommentActivityDeleteEvent 리스너 실행: {}", event);
        deleteUserActivity(event.userId(), event.commentId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUpdateEvent(CommentActivityUpdateEvent event) {
        log.debug("CommentActivityUpdateEvent 리스너 실행: {}", event);

        updateUserActivity(event.userId(), event.commentId(), event.commentActivityDto());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserNameUpdateEvent(UserNameActivityUpdateEvent event) {
        UUID userId = event.userId();
        String newUserName = event.newUserName();

        log.debug("댓글 활동 UserNameUpdateEvent 리스너 실행 userId={}, newUserName={}", userId,
            newUserName);

        Query query = new Query(Criteria.where("comments.userId").is(userId));

        Update update = new Update().set("comments.$[].userNickname", newUserName);

        UpdateResult result = mongoTemplate.updateMulti(query, update, CommentActivity.class);

        log.debug("댓글 활동 UserNameUpdateEvent 리스너 실행 완료 userId={}, 수정된 문서 수={}", userId,
            result.getModifiedCount());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentLikeCountUpdateEvent(CommentLikeCountActivityUpdateEvent event) {
        UUID commentId = event.commentId();
        long newLikeCount = event.newLikeCount();

        log.debug("CommentLikeCountUpdateEvent 리스너 실행: commentId={}, newLikeCount={}", commentId, newLikeCount);

        Query query = new Query(Criteria.where("comments.id").is(commentId));
        Update update = new Update().set("comments.$.likeCount", newLikeCount);

        UpdateResult result = mongoTemplate.updateMulti(query, update, CommentActivity.class);

        log.debug("CommentLikeCountUpdateEvent 리스너 실행 완료 : commentId={}, 업데이트된 문서 수={}", commentId, result.getModifiedCount());
    }
}
