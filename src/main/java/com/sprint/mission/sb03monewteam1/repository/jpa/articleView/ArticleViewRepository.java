package com.sprint.mission.sb03monewteam1.repository.jpa.articleView;

import com.sprint.mission.sb03monewteam1.entity.ArticleView;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ArticleViewRepository extends JpaRepository<ArticleView, UUID> {

    boolean existsByUserIdAndArticleId(UUID userId, UUID articleId);

    List<ArticleView> findByUserIdAndArticleId(UUID userId, UUID articleId);

    @Transactional
    void deleteByArticleId(UUID articleId);
}
