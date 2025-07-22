package com.sprint.mission.sb03monewteam1.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.sprint.mission.sb03monewteam1.entity.base.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "articles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article extends BaseEntity {

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
    private Long commentCount;

    @Column(name = "view_count", nullable = false)
    private Long viewCount;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ArticleView> articleViews;

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

    public boolean isDeleted() {
        return this.isDeleted;
    }

    public boolean isNotDeleted() {
        return !this.isDeleted;
    }

    @Builder
    public Article(
        UUID id,
        String source,
        String sourceUrl,
        String title,
        Instant publishDate,
        String summary,
        Long commentCount,
        Long viewCount,
        Boolean isDeleted,
        List<ArticleView> articleViews
    ) {
        super();
        if (id != null) {
            assignId(id);
        }
        this.source = source;
        this.sourceUrl = sourceUrl;
        this.title = title;
        this.publishDate = publishDate;
        this.summary = summary;
        this.commentCount = commentCount != null ? commentCount : 0L;
        this.viewCount = viewCount != null ? viewCount : 0L;
        this.isDeleted = isDeleted != null ? isDeleted : false;
        this.articleViews = articleViews != null ? articleViews : new ArrayList<>();
    }
}
