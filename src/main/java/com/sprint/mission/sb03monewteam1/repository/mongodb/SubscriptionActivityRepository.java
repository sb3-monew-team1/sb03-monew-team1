package com.sprint.mission.sb03monewteam1.repository.mongodb;

import com.sprint.mission.sb03monewteam1.document.SubscriptionActivity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SubscriptionActivityRepository extends MongoRepository<SubscriptionActivity, UUID> {

    List<SubscriptionActivity> findAllBySubscriptions_interestId(UUID interestId);
}