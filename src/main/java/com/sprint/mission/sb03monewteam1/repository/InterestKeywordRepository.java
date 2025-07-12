package com.sprint.mission.sb03monewteam1.repository;

import com.sprint.mission.sb03monewteam1.entity.InterestKeyword;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterestKeywordRepository extends JpaRepository<InterestKeyword, UUID> {

    boolean existsByKeywordAndInterestName(String keyword, String interestName);
}
