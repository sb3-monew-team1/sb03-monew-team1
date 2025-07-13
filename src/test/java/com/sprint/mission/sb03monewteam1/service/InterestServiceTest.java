package com.sprint.mission.sb03monewteam1.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.sb03monewteam1.dto.request.InterestRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.InterestDto;
import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.exception.interest.InterestDuplicateException;
import com.sprint.mission.sb03monewteam1.exception.interest.InterestSimilarityException;
import com.sprint.mission.sb03monewteam1.fixture.InterestFixture;
import com.sprint.mission.sb03monewteam1.mapper.InterestMapper;
import com.sprint.mission.sb03monewteam1.repository.InterestRepository;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("InterestService 테스트")
class InterestServiceTest {

    @Mock
    private InterestRepository interestRepository;

    @Mock
    private InterestMapper interestMapper;

    @InjectMocks
    private InterestServiceImpl interestService;

    @Nested
    @DisplayName("관심사 생성 테스트")
    class InterestCreateTests {

        @Test
        void 관심사를_등록하면_관심사_응답_DTO를_반환한다() {

            // Given
            InterestRegisterRequest request = InterestFixture.createInterestRegisterRequest();
            Interest savedInterest = new Interest();
            InterestDto expectedResponse = InterestFixture.createInterestResponseDto();

            given(interestRepository.existsByName(request.name())).willReturn(false);
            given(interestRepository.save(any(Interest.class))).willReturn(savedInterest);
            given(interestMapper.toDto(savedInterest, false)).willReturn(expectedResponse);

            // When
            InterestDto result = interestService.create(request);

            // Then
            assertThat(result)
                .isNotNull()
                .extracting(InterestDto::name)
                .isEqualTo(expectedResponse.name());

            then(interestRepository).should().existsByName(request.name());
            then(interestRepository).should().save(any(Interest.class));
            then(interestMapper).should().toDto(savedInterest, false);
        }

        @Test
        void 중복된_관심사_이름이면_InterestDuplicateException이_발생한다() {
            // Given
            InterestRegisterRequest request = InterestFixture.createInterestRegisterRequest();
            given(interestRepository.existsByName(request.name())).willReturn(true);

            // When
            Throwable throwable = catchThrowable(() -> interestService.create(request));

            // Then
            assertThat(throwable).isInstanceOf(InterestDuplicateException.class);

            then(interestRepository).should().existsByName(request.name());
            then(interestRepository).shouldHaveNoMoreInteractions();
            then(interestMapper).shouldHaveNoInteractions();
        }

        @Test
        void 관심사_이름_유사도가_80_퍼센트_이상일_경우_InterestSimilarityException이_발생한다() {

            // Given
            InterestRegisterRequest request = InterestFixture.createInterestRegisterRequest();
            InterestRegisterRequest similarRequest = InterestFixture.createInterestRegisterRequestWithSimilarName();

            Interest existingInterest = new Interest();
            existingInterest.setName(request.name());
            given(interestRepository.findAll()).willReturn(List.of(existingInterest));

            // When
            Throwable throwable = catchThrowable(() -> interestService.create(similarRequest));

            // Then
            assertThat(throwable).isInstanceOf(InterestSimilarityException.class)
                .hasMessageContaining("유사한 관심사 이름이 존재합니다.");
        }
    }

    @Nested
    @DisplayName("관심사 조회 테스트")
    class InterestReadTests {

        private static final UUID REQUEST_USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        @Test
        void 검색어로_관심사_이름과_키워드에_부분일치하는_관심사를_검색한다() throws Exception {
            // Given
            String searchKeyword = "football";
            String cursor = null;
            int limit = 10;
            String sortBy = "name";
            String sortDirection = "asc";

            List<InterestDto> interests = InterestFixture.createInterestDtoList();

            given(interestRepository.searchByKeywordOrName(
                REQUEST_USER_ID, searchKeyword, cursor, limit, sortBy, sortDirection))
                .willReturn(interests);

            // When
            List<InterestDto> result = interestService.search(
                REQUEST_USER_ID, searchKeyword, cursor, limit, sortBy, sortDirection
            );

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).name()).isEqualTo("football club");
            assertThat(result.get(1).name()).isEqualTo("soccer");

            then(interestRepository).should()
                .searchByKeywordOrName(REQUEST_USER_ID, searchKeyword, cursor, limit, sortBy, sortDirection);
        }

        @Test
        void 관심사를_조회하면_이름으로_정렬한다() throws Exception {
            // Given
            String searchKeyword = null;
            String cursor = null;
            int limit = 10;
            String sortBy = "name";
            String sortDirection = "asc";

            List<InterestDto> interests = InterestFixture.createInterestDtoList();

            given(interestRepository.searchByKeywordOrName(
                REQUEST_USER_ID, searchKeyword, cursor, limit, sortBy, sortDirection))
                .willReturn(interests);

            // When
            List<InterestDto> result = interestService.search(
                REQUEST_USER_ID, searchKeyword, cursor, limit, sortBy, sortDirection
            );

            // Then
            assertThat(result).hasSize(4);
            assertThat(result.get(0).name()).isEqualTo("aesthetic");
            assertThat(result.get(1).name()).isEqualTo("beauty");
            assertThat(result.get(2).name()).isEqualTo("football club");
            assertThat(result.get(3).name()).isEqualTo("soccer");

            then(interestRepository).should()
                .searchByKeywordOrName(REQUEST_USER_ID, searchKeyword, cursor, limit, sortBy, sortDirection);
        }

        @Test
        void 관심사를_조회하면_구독자_수로_정렬한다() throws Exception {
            // Given
            String searchKeyword = null;
            String cursor = null;
            int limit = 10;
            String sortBy = "subscriberCount";
            String sortDirection = "desc";

            List<InterestDto> interests = InterestFixture.createInterestDtoList();

            given(interestRepository.searchByKeywordOrName(
                REQUEST_USER_ID, searchKeyword, cursor, limit, sortBy, sortDirection))
                .willReturn(interests);

            // When
            List<InterestDto> result = interestService.search(
                REQUEST_USER_ID, searchKeyword, cursor, limit, sortBy, sortDirection
            );

            // Then
            assertThat(result).hasSize(4);
            assertThat(result.get(0).name()).isEqualTo("football club");
            assertThat(result.get(0).subscriberCount()).isEqualTo(200L);
            assertThat(result.get(1).name()).isEqualTo("soccer");
            assertThat(result.get(1).subscriberCount()).isEqualTo(150L);
            assertThat(result.get(2).name()).isEqualTo("aesthetic");
            assertThat(result.get(2).subscriberCount()).isEqualTo(100L);
            assertThat(result.get(3).name()).isEqualTo("beauty");
            assertThat(result.get(3).subscriberCount()).isEqualTo(50L);

            then(interestRepository).should()
                .searchByKeywordOrName(REQUEST_USER_ID, searchKeyword, cursor, limit, sortBy, sortDirection);
        }
    }
}
