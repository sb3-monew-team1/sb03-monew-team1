package com.sprint.mission.sb03monewteam1.repository.jpa;

import com.sprint.mission.sb03monewteam1.entity.Interest;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InterestRepository extends JpaRepository<Interest, UUID>, InterestRepositoryCustom {

    boolean existsByName(String name);

    long countByKeywordOrName(String keyword);

    @Query("SELECT DISTINCT i FROM Interest i LEFT JOIN FETCH i.keywords")
    List<Interest> findAllWithKeywords();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Interest i "
        + "set i.subscriberCount = CASE WHEN i.subscriberCount > 0 THEN i.subscriberCount - 1 ELSE 0 END "
        + "WHERE i.id = :interestId")
    void decrementSubscriberCount(@Param("interestId") UUID interestId);
}
