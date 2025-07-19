package com.sprint.mission.sb03monewteam1.event.listener;

import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.repository.MongoRepository;

@Slf4j
public abstract class AbstractActivityEventListener<T, D, R extends MongoRepository<D, UUID>> {

    protected abstract UUID getActivityId(T dto);
    protected abstract R getRepository();
    protected abstract List<T> getActivityList(D document);
    protected abstract D createNewDocument(UUID userId);

    public void saveUserActivity(UUID userId, T dto) {
        UUID activityId = getActivityId(dto);

        log.debug("ActivityCreate 요청: userId: {}, activityId: {}, dto: {}", userId, activityId, dto);

        D document = getRepository().findById(userId).orElseGet(() -> createNewDocument(userId));
        getActivityList(document).add(dto);
        getRepository().save(document);

        log.debug("ActivityCreate 성공 userId: {}", userId);
    }

    public void deleteUserActivity(UUID userId, UUID activityId) {
        log.debug("ActivityDelete 요청: userId: {}, activityId: {}", userId, activityId);

        D document = getRepository().findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Document not found for userId: " + userId));

        List<T> activities = getActivityList(document);
        int beforeSize = activities.size();

        activities.removeIf(activity -> getActivityId(activity).equals(activityId));

        getRepository().save(document);

        log.debug("ActivityDelete 성공 userId: {}, 삭제된 개수: {}", userId, beforeSize - activities.size());
    }
}
