package com.sprint.mission.sb03monewteam1.entity;

import com.sprint.mission.sb03monewteam1.entity.base.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "interests")
@Getter @Setter
public class Interest extends BaseUpdatableEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long subscriberCount = 0L;

    @OneToMany(mappedBy = "interest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InterestKeyword> keywords = new ArrayList<>();
}
