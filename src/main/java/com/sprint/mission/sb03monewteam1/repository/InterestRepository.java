package com.sprint.mission.sb03monewteam1.repository;

import com.sprint.mission.sb03monewteam1.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface InterestRepository extends JpaRepository<Interest, UUID>, InterestRepositoryCustom {

    boolean existsByName(String name);

}
