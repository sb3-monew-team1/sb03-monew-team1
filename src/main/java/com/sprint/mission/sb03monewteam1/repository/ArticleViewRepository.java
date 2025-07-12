package com.sprint.mission.sb03monewteam1.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sprint.mission.sb03monewteam1.entity.ArticleView;

@Repository
public interface ArticleViewRepository extends JpaRepository<ArticleView, UUID> {

    // 사용자-기사 조합으로 중복 체크
    Optional<ArticleView> findByUserIdAndArticleId(UUID userId, UUID articleId);

    // 사용자-기사 조합 존재 여부 확인
    boolean existsByUserIdAndArticleId(UUID userId, UUID articleId);
}
