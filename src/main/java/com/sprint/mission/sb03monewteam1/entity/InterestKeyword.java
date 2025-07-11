package com.sprint.mission.sb03monewteam1.entity;

import com.sprint.mission.sb03monewteam1.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "interest_keywords")
@Getter @Setter
public class InterestKeyword extends BaseEntity {

    @Column(nullable = false)
    private String keyword;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_id", nullable = false)
    private Interest interest;
}