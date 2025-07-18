package com.sprint.mission.sb03monewteam1.repository.jpa;

import com.sprint.mission.sb03monewteam1.entity.Comment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID>, CommentRepositoryCustom {

    Long countByArticleId(UUID articleId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Comment c " +
        "SET c.likeCount = CASE WHEN c.likeCount > 0 THEN c.likeCount - 1 ELSE 0 END " +
        "WHERE c.id = :commentId")
    void decreaseLikeCountById(@Param("commentId") UUID commentId);

    Optional<Comment> findByIdAndIsDeletedFalse(UUID commentId);

    List<Comment> findAllByArticleId(UUID articleId);

    List<Comment> findByAuthorId(UUID userId);
}
