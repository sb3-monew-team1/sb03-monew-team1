package com.sprint.mission.sb03monewteam1.repository;

import com.sprint.mission.sb03monewteam1.entity.CommentLike;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, UUID> {

    @Query("SELECT c FROM CommentLike c LEFT JOIN FETCH c.comment WHERE c.user.id = :userId")
    List<CommentLike> findAllByUserId(@Param("userId") UUID userId);

    void deleteByUserId(UUID userId);

    void deleteByCommentId(UUID commentId);
}
