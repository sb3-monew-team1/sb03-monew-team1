package com.sprint.mission.sb03monewteam1.repository.jpa.interest;

import java.util.List;
import com.sprint.mission.sb03monewteam1.entity.Interest;

public interface InterestRepositoryCustom {

    List<Interest> searchByKeywordOrName(
        String searchKeyword,
        String cursor,
        int limit,
        String sortBy,
        String sortDirection
    );

    long countByKeywordOrName(String keyword);
}
