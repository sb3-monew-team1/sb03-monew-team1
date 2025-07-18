package com.sprint.mission.sb03monewteam1.repository.jpa;

import com.sprint.mission.sb03monewteam1.entity.ArticleInterest;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ArticleInterestRepository extends JpaRepository<ArticleInterest, UUID> {

    @Transactional
    void deleteByArticleId(UUID articleId);
}
