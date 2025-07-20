package com.sprint.mission.sb03monewteam1.repository.jpa.interest;

import com.sprint.mission.sb03monewteam1.entity.InterestKeyword;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InterestKeywordRepository extends JpaRepository<InterestKeyword, UUID> {

    boolean existsByKeywordAndInterestName(String keyword, String interestName);

    /**
     * 모든 InterestKeyword 엔티티에서 중복되지 않는 키워드 문자열 목록을 조회합니다.
     *
     * @return 중복을 제거한 키워드 문자열의 리스트
     */
    @Query("SELECT DISTINCT k.keyword FROM InterestKeyword k")
    List<String> findAllDistinct();

    /**
 * 지정된 키워드와 일치하는 모든 InterestKeyword 엔티티 목록을 반환합니다.
 *
 * @param keyword 검색할 키워드
 * @return 해당 키워드와 일치하는 InterestKeyword 엔티티 리스트
 */
List<InterestKeyword> findAllByKeyword(String keyword);

    /**
 * 지정된 interestId와 연관된 모든 InterestKeyword 엔티티를 삭제합니다.
 *
 * @param interestId 삭제할 InterestKeyword 엔티티와 연결된 관심사(Interest)의 UUID
 */
void deleteByInterestId(UUID interestId);
}
