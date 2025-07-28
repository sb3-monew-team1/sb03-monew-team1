package com.sprint.mission.sb03monewteam1.repository.jpa.articleView;

import com.sprint.mission.sb03monewteam1.entity.ArticleView;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ArticleViewRepository extends JpaRepository<ArticleView, UUID> {

    boolean existsByUserIdAndArticleId(UUID userId, UUID articleId);

    List<ArticleView> findByUserIdAndArticleId(UUID userId, UUID articleId);

    @org.springframework.data.jpa.repository.Query("""
        SELECT av.article.id FROM ArticleView av
        WHERE av.userId = :userId AND av.article.id IN :articleIds
        """)
    Set<UUID> findViewedArticleIdsByUserIdAndArticleIds(
        @org.springframework.data.repository.query.Param("userId") java.util.UUID userId,
        @org.springframework.data.repository.query.Param("articleIds") java.util.List<java.util.UUID> articleIds
    );

    @Transactional
    void deleteByArticleId(UUID articleId);
}
