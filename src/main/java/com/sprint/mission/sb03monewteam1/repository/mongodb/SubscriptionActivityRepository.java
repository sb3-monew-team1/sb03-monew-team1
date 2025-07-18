package com.sprint.mission.sb03monewteam1.repository.mongodb;

import com.sprint.mission.sb03monewteam1.document.SubscriptionActivity;
import java.util.UUID;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;

@Document(collection = "subscription_activities")
public interface SubscriptionActivityRepository extends MongoRepository<SubscriptionActivity, UUID> {
}
