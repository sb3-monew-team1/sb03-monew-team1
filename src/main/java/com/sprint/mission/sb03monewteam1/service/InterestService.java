package com.sprint.mission.sb03monewteam1.service;

import com.sprint.mission.sb03monewteam1.dto.InterestDto;
import com.sprint.mission.sb03monewteam1.dto.SubscriptionDto;
import com.sprint.mission.sb03monewteam1.dto.request.InterestRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponse;
import java.util.UUID;

public interface InterestService {

    InterestDto create(InterestRegisterRequest request);

    /**
         * 사용자의 관심사를 검색어, 정렬, 페이징 조건에 따라 조회하여 반환합니다.
         *
         * @param userId        관심사를 조회할 사용자 ID
         * @param searchKeyword 관심사 검색에 사용할 키워드
         * @param cursor        페이징을 위한 커서 값
         * @param limit         한 번에 조회할 관심사 개수
         * @param sortBy        정렬 기준 필드명
         * @param sortDirection 정렬 방향(오름차순 또는 내림차순)
         * @return 조건에 맞는 관심사 목록과 페이징 정보를 담은 응답 객체
         */
        CursorPageResponse<InterestDto> getInterests(
        UUID userId,
        String searchKeyword,
        String cursor,
        int limit,
        String sortBy,
        String sortDirection);

    /**
 * 사용자를 특정 관심사에 구독시켜 새로운 구독 정보를 생성합니다.
 *
 * @param interestId 구독할 관심사의 UUID
 * @param userId     구독하는 사용자의 UUID
 * @return 생성된 구독 정보를 담은 SubscriptionDto
 */
SubscriptionDto createSubscription(UUID interestId, UUID userId);

    /**
 * 지정된 관심사(interest)를 삭제합니다.
 *
 * @param interestId 삭제할 관심사의 UUID
 */
void deleteInterest(UUID interestId);
}
