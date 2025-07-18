package com.sprint.mission.sb03monewteam1.repository;

import com.sprint.mission.sb03monewteam1.entity.InterestKeyword;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InterestKeywordRepository extends JpaRepository<InterestKeyword, UUID> {

    boolean existsByKeywordAndInterestName(String keyword, String interestName);

    @Query("SELECT DISTINCT k.keyword FROM InterestKeyword k")
    List<String> findAllDistinct();

    List<InterestKeyword> findAllByKeyword(String keyword);
}
