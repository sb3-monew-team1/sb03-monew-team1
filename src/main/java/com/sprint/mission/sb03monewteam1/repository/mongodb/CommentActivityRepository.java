package com.sprint.mission.sb03monewteam1.repository.mongodb;

import com.sprint.mission.sb03monewteam1.document.CommentActivity;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.UUID;

@Document(collection = "comment_activities")
public interface CommentActivityRepository extends MongoRepository<CommentActivity, UUID> {
}



