package com.sprint.mission.sb03monewteam1.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sprint.mission.sb03monewteam1.entity.Article;

@Repository
public interface ArticleRepository extends JpaRepository<Article, UUID>, ArticleRepositoryCustom {

    Optional<Article> findByIdAndIsDeletedFalse(UUID id);

    boolean existsBySourceUrl(String sourceUrl);
}
