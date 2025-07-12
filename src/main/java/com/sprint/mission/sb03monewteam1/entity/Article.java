package com.sprint.mission.sb03monewteam1.entity;

import com.sprint.mission.sb03monewteam1.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Entity
@Table(name = "news_article")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article extends BaseEntity {

    @Column(name = "source", nullable = false, length = 50)
    private String source;

    @Column(name = "source_url", nullable = false)
    private String sourceUrl;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "publish_date", nullable = false)
    private Instant publishDate;

    @Column(name = "summary")
    private String summary;

    @Column(name = "comment_count", nullable = false)
    private Long commentCount;

    @Column(name = "view_count", nullable = false)
    private Long viewCount;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;
}
