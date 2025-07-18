package com.sprint.mission.sb03monewteam1.repository.mongodb;

import com.sprint.mission.sb03monewteam1.document.CommentLikeActivity;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

@Document(collection = "comment_like_activities")
public interface CommentLikeActivityRepository extends MongoRepository<CommentLikeActivity, UUID> {
}
