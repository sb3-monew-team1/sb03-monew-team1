package com.sprint.mission.sb03monewteam1.repository;

import java.util.List;
import java.util.UUID;

import com.sprint.mission.sb03monewteam1.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InterestRepository extends JpaRepository<Interest, UUID>, InterestRepositoryCustom {

    boolean existsByName(String name);

    @Query("SELECT DISTINCT i FROM Interest i LEFT JOIN FETCH i.keywords")
    List<Interest> findAllWithKeywords();
}
