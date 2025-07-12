package com.sprint.mission.sb03monewteam1.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Entity
@Table(name = "news_views", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "news_article_id" }))
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleView {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_article_id", nullable = false)
    private Article article;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // 생성 시점에 createdAt 설정
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    // 팩토리 메서드
    public static ArticleView createArticleView(UUID userId, Article article) {
        return ArticleView.builder()
                .userId(userId)
                .article(article)
                .build();
    }
}
