package com.sprint.mission.sb03monewteam1.repository.jpa.articleInterest;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sprint.mission.sb03monewteam1.entity.ArticleInterest;

@Repository
public interface ArticleInterestRepository extends JpaRepository<ArticleInterest, UUID> {

    @Transactional
    void deleteByArticleId(UUID articleId);

    @Transactional
    long deleteByInterestId(UUID interestId);

    @Query("select new org.apache.commons.lang3.tuple.ImmutablePair(ai.article.id, ai.interest.id) " +
           "from ArticleInterest ai " +
           "where ai.article.id in :articleIds and ai.interest.id in :interestIds")
    Set<Pair<UUID, UUID>> findExistingArticleInterestPairsAsPairs(
        @Param("articleIds") List<UUID> articleIds,
        @Param("interestIds") List<UUID> interestIds
    );
}
