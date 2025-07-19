package com.sprint.mission.sb03monewteam1.repository.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sprint.mission.sb03monewteam1.entity.InterestKeyword;

public interface InterestKeywordRepository extends JpaRepository<InterestKeyword, UUID> {

    boolean existsByKeywordAndInterestName(String keyword, String interestName);

    @Query("SELECT DISTINCT k.keyword FROM InterestKeyword k")
    List<String> findAllDistinct();

    List<InterestKeyword> findAllByKeyword(String keyword);

    Optional<InterestKeyword> findByKeyword(String keyword);
}
