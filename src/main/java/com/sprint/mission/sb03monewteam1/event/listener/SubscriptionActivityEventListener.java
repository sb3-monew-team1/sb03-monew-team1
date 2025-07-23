package com.sprint.mission.sb03monewteam1.event.listener;

import com.mongodb.client.result.UpdateResult;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import com.sprint.mission.sb03monewteam1.document.SubscriptionActivity;
import com.sprint.mission.sb03monewteam1.dto.SubscriptionDto;
import com.sprint.mission.sb03monewteam1.event.SubscriptionActivityBulkDeleteEvent;
import com.sprint.mission.sb03monewteam1.event.SubscriptionActivityCreateEvent;
import com.sprint.mission.sb03monewteam1.event.SubscriptionActivityDeleteEvent;
import com.sprint.mission.sb03monewteam1.event.SubscriptionActivityKeywordUpdateEvent;
import com.sprint.mission.sb03monewteam1.repository.mongodb.SubscriptionActivityRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionActivityEventListener extends AbstractActivityEventListener<
    SubscriptionDto,
    SubscriptionActivity,
    SubscriptionActivityRepository> {

    private final MongoTemplate mongoTemplate;

    private final SubscriptionActivityRepository repository;

    @Override
    protected UUID getActivityId(SubscriptionDto dto) {
        return dto.interestId();
    }

    @Override
    protected SubscriptionActivityRepository getRepository() {
        return repository;
    }

    @Override
    protected List<SubscriptionDto> getActivityList(SubscriptionActivity document) {
        return document.getSubscriptions();
    }

    @Override
    protected SubscriptionActivity createNewDocument(UUID userId) {
        SubscriptionActivity activity = new SubscriptionActivity();
        activity.setUserId(userId);
        activity.setSubscriptions(new ArrayList<>());
        return activity;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCreateEvent(SubscriptionActivityCreateEvent event) {
        log.debug("SubscriptionActivityCreateEvent 리스너 실행: {}", event);
        saveUserActivity(event.userId(), event.subscriptionDto());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDeleteEvent(SubscriptionActivityDeleteEvent event) {
        log.debug("SubscriptionActivityDeleteEvent 리스너 실행: {}", event);
        deleteUserActivity(event.userId(), event.interestId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleKeywordUpdateEvent(SubscriptionActivityKeywordUpdateEvent event) {
        UUID interestId = event.interestId();
        List<String> keywords = event.newKeywords();

        log.debug("handleKeywordUpdateEvent 리스너 실행: interestId={}, newKeywords={}", interestId, keywords);

        Query query = new Query(Criteria.where("subscriptions.interestId").is(interestId));
        log.debug("[쿼리 생성] query={}", query);

        Update update = new Update().set("subscriptions.$.interestKeywords", keywords);
        log.debug("[업데이트 정의] update={}", update.getUpdateObject());

        List<SubscriptionActivity> matchedDocs = mongoTemplate.find(query, SubscriptionActivity.class);
        log.debug("[문서 검색] 조건에 맞는 문서 수={}", matchedDocs.size());

        UpdateResult result = mongoTemplate.updateMulti(query, update, SubscriptionActivity.class);
        log.info("handleKeywordUpdateEvent 리스너 실행 완료: interestId={}, 수정 문서 수={}", interestId, result.getModifiedCount());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBulkDeleteEvent(SubscriptionActivityBulkDeleteEvent event) {
        UUID interestId = event.interestId();
        log.debug("SubscriptionActivityBulkDeleteEvent 리스너 실행: interestId={}", interestId);

        List<SubscriptionActivity> allDocs = repository.findAllBySubscriptions_interestId(
            interestId);
        for (SubscriptionActivity doc : allDocs) {
            deleteUserActivity(doc.getUserId(), interestId);
        }
    }
}
