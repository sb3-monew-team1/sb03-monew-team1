package com.sprint.mission.sb03monewteam1.event.listener;

import com.sprint.mission.sb03monewteam1.document.SubscriptionActivity;
import com.sprint.mission.sb03monewteam1.dto.SubscriptionActivityDto;
import com.sprint.mission.sb03monewteam1.event.SubscriptionActivityCreateEvent;
import com.sprint.mission.sb03monewteam1.event.SubscriptionActivityDeleteEvent;
import com.sprint.mission.sb03monewteam1.repository.mongodb.SubscriptionActivityRepository;
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
public class SubscriptionActivityEventListener extends AbstractActivityEventListener<
    SubscriptionActivityDto,
    SubscriptionActivity,
    SubscriptionActivityRepository> {

    private final SubscriptionActivityRepository repository;

    @Override
    protected UUID getUserId(SubscriptionActivityDto dto) {
        return dto.id();
    }

    @Override
    protected UUID getActivityId(SubscriptionActivityDto dto) {
        return dto.interestId();
    }

    @Override
    protected SubscriptionActivityRepository getRepository() {
        return repository;
    }

    @Override
    protected List<SubscriptionActivityDto> getActivityList(SubscriptionActivity document) {
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
    public void handle(SubscriptionActivityCreateEvent event) {
        log.debug("SubscriptionActivityCreateEvent 리스너 실행: {}", event);
        saveUserActivity(event.subscriptionActivityDto());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDeleteEvent(SubscriptionActivityDeleteEvent event) {
        log.debug("SubscriptionActivityDeleteEven 리스너 실행: {}", event);
        deleteUserActivity(event.id(), event.interestId());
    }
}
