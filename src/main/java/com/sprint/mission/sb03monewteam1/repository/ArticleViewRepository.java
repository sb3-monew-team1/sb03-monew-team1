package com.sprint.mission.sb03monewteam1.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sprint.mission.sb03monewteam1.entity.ArticleView;

@Repository
public interface ArticleViewRepository extends JpaRepository<ArticleView, UUID> {

    boolean existsByUserIdAndArticleId(UUID userId, UUID articleId);

    List<ArticleView> findByUserIdAndArticleId(UUID userId, UUID articleId);
}
