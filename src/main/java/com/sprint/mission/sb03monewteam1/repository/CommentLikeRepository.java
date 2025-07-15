package com.sprint.mission.sb03monewteam1.repository;

import com.sprint.mission.sb03monewteam1.entity.CommentLike;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, UUID> {


}
