package com.sprint.mission.sb03monewteam1.repository.jpa;

import com.sprint.mission.sb03monewteam1.entity.Article;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, UUID>, ArticleRepositoryCustom {

    Optional<Article> findByIdAndIsDeletedFalse(UUID id);

    @Query("select a.sourceUrl from Article a where a.sourceUrl in :urls and a.isDeleted = false")
    List<String> findAllBySourceUrlIn(Collection<String> urls);

    boolean existsBySourceUrl(String sourceUrl);
}