package com.sprint.mission.sb03monewteam1.event.listener;

import com.mongodb.client.result.UpdateResult;
import com.sprint.mission.sb03monewteam1.document.CommentLikeActivity;
import com.sprint.mission.sb03monewteam1.dto.CommentActivityDto;
import com.sprint.mission.sb03monewteam1.dto.CommentLikeActivityDto;
import com.sprint.mission.sb03monewteam1.event.CommentActivityUpdateEvent;
import com.sprint.mission.sb03monewteam1.event.CommentLikeActivityCreateEvent;
import com.sprint.mission.sb03monewteam1.event.CommentLikeActivityDeleteEvent;
import com.sprint.mission.sb03monewteam1.event.UserNameUpdateEvent;
import com.sprint.mission.sb03monewteam1.repository.mongodb.CommentLikeActivityRepository;
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
public class CommentLikeActivityEventListener extends AbstractActivityEventListener<
    CommentLikeActivityDto,
    CommentLikeActivity,
    CommentLikeActivityRepository> {

    private final MongoTemplate mongoTemplate;

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

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserNameUpdateForLikes(UserNameUpdateEvent event) {
        UUID userId = event.userId();
        String newUserName = event.newUserName();

        log.debug("댓글 좋아요 활동 UserNameUpdateEvent 리스너 실행 userId={}, newUserName={}", userId,
            newUserName);

        Query query = new Query(Criteria.where("commentLikes.commentUserId").is(userId));

        Update update = new Update().set("commentLikes.$.commentUserNickname", newUserName);

        UpdateResult result = mongoTemplate.updateMulti(query, update, CommentLikeActivity.class);

        log.debug("[업데이트 실행] userId={}, 수정된 문서 수={}", userId, result.getModifiedCount());

        if (result.getModifiedCount() == 0) {
            log.warn("[업데이트 실패] userId={}에 대한 댓글 좋아요 수정이 적용되지 않았습니다.", userId);
        } else {
            log.info("댓글 좋아요 활동 UserNameUpdateEvent 리스너 실행 완료 userId={}, 수정된 문서 수={}", userId,
                result.getModifiedCount());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentActivityUpdateForLikes(CommentActivityUpdateEvent event) {
        UUID commentId = event.commentId();
        String updatedContent = event.commentActivityDto().content();

        log.debug("댓글 좋아요 활동 CommentActivityUpdateEvent 리스너 실행 commentId={}, updatedContent={}",
            commentId, updatedContent);

        Query query = new Query(Criteria.where("commentLikes.commentId").is(commentId));

        Update update = new Update().set("commentLikes.$.commentContent", updatedContent);

        UpdateResult result = mongoTemplate.updateMulti(query, update, CommentLikeActivity.class);

        log.debug("[업데이트 실행] commentId={}, 수정된 문서 수={}", commentId, result.getModifiedCount());

        if (result.getModifiedCount() == 0) {
            log.warn("[업데이트 실패] commentId={}에 대한 댓글 내용 수정이 적용되지 않았습니다.", commentId);
        } else {
            log.info("댓글 좋아요 활동 CommentActivityUpdateEvent 리스너 실행 완료 commentId={}, 수정된 문서 수={}",
                commentId, result.getModifiedCount());
        }
    }
}
