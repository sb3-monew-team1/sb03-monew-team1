package com.sprint.mission.sb03monewteam1.repository.jpa;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sprint.mission.sb03monewteam1.entity.Article;

@Repository
public interface ArticleRepository extends JpaRepository<Article, UUID>, ArticleRepositoryCustom {

    Optional<Article> findByIdAndIsDeletedFalse(UUID id);

    List<String> findAllBySourceUrlIn(Collection<String> urls);

    boolean existsBySourceUrl(String sourceUrl);
}
