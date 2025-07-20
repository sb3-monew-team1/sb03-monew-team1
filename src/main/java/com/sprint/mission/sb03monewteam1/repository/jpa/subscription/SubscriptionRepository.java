package com.sprint.mission.sb03monewteam1.repository.jpa.subscription;

import com.sprint.mission.sb03monewteam1.entity.Subscription;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    @Query("SELECT s FROM Subscription s LEFT JOIN FETCH s.interest WHERE s.user.id = :userId")
    List<Subscription> findAllByUserId(@Param("userId") UUID userId);

    @Transactional
    void deleteByUserId(UUID userId);

    Optional<Subscription> findByInterestId(UUID interestId);

    /**
     * 지정된 관심사 ID에 해당하는 모든 구독 엔티티를 조회하며, 관련된 사용자 정보를 함께 즉시 로딩합니다.
     *
     * @param interestId 조회할 관심사(Interest)의 ID
     * @return 관심사 ID에 연결된 구독(Subscription) 목록
     */
    @Query("SELECT s FROM Subscription s LEFT JOIN FETCH s.user WHERE s.interest.id = :interestId")
    List<Subscription> findAllByInterestIdFetchUser(@Param("interestId") UUID interestId);

    /**
 * 지정된 관심사 ID와 연관된 모든 구독 엔티티를 삭제합니다.
 *
 * @param interestId 삭제할 구독의 기준이 되는 관심사 ID
 */
void deleteByInterestId(UUID interestId);
}
