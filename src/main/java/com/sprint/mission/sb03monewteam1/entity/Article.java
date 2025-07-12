package com.sprint.mission.sb03monewteam1.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Entity
@Table(name = "news_articles")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "source", nullable = false, length = 50)
    private String source;

    @Column(name = "source_url", nullable = false, unique = true, columnDefinition = "TEXT")
    private String sourceUrl;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "publish_date", nullable = false)
    private Instant publishDate;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "comment_count", nullable = false)
    @Builder.Default
    private Long commentCount = 0L;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Long viewCount = 0L;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // ArticleView와의 연관관계
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ArticleView> articleViews = new ArrayList<>();

    // 생성 시점에 createdAt 설정
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    // 비즈니스 메서드
    public void increaseViewCount() {
        this.viewCount++;
    }

    public void increaseCommentCount() {
        this.commentCount++;
    }

    public void decreaseCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }

    public void markAsDeleted() {
        this.isDeleted = true;
    }

    public void restore() {
        this.isDeleted = false;
    }

    // 조회용 메서드
    public boolean isDeleted() {
        return this.isDeleted;
    }

    public boolean isNotDeleted() {
        return !this.isDeleted;
    }
}
