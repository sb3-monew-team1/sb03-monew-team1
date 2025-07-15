package com.sprint.mission.sb03monewteam1.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sprint.mission.sb03monewteam1.dto.InterestDto;
import com.sprint.mission.sb03monewteam1.dto.request.InterestRegisterRequest;
import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.exception.interest.InterestDuplicateException;
import com.sprint.mission.sb03monewteam1.exception.interest.InterestSimilarityException;
import com.sprint.mission.sb03monewteam1.fixture.InterestFixture;
import com.sprint.mission.sb03monewteam1.mapper.InterestMapper;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponse;


import com.sprint.mission.sb03monewteam1.repository.InterestRepository;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
@Slf4j
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

        @Test
        void 관심사를_조회하면_구독자수로_정렬한다() {
            // Given
            int limit = 10;
            String orderBy = "subscriberCount";
            String direction = "desc";

            Interest interest1 = Interest.builder().name("aesthetic").subscriberCount(150L).build();
            Interest interest2 = Interest.builder().name("soccer").subscriberCount(200L).build();

            List<Interest> interests = Arrays.asList(interest2, interest1); // 정렬 기준에 맞게 정렬된 상태
            List<InterestDto> interestDtos = Arrays.asList(
                InterestDto.builder().name("soccer").subscriberCount(200).build(),
                InterestDto.builder().name("aesthetic").subscriberCount(150).build()
            );

            when(interestRepository.searchByKeywordOrName(eq(null), eq(null), eq(limit + 1), eq(orderBy), eq(direction)))
                .thenReturn(interests);

            when(interestMapper.toDto(eq(interest2), eq(true))).thenReturn(interestDtos.get(0));
            when(interestMapper.toDto(eq(interest1), eq(true))).thenReturn(interestDtos.get(1));

            // When
            CursorPageResponse<InterestDto> result = interestService.getInterests(null, null, limit, orderBy, direction);

            // Then
            assertThat(result.content()).hasSize(2); // 결과의 크기가 2인지 확인
            assertThat(result.content().get(0).name()).isEqualTo("soccer");
            assertThat(result.content().get(1).name()).isEqualTo("aesthetic");
            assertThat(result.hasNext()).isFalse();

            // verify
            verify(interestRepository).searchByKeywordOrName(eq(null), eq(null), eq(limit + 1), eq(orderBy), eq(direction)); // searchByKeywordOrName 메서드 호출 확인
        }

        @Test
        void 관심사를_조회하면_키워드로_검색한다() {
            // Given
            int limit = 10;
            String searchKeyword = "soccer";
            String orderBy = "name";
            String direction = "asc";

            Interest interest1 = Interest.builder().name("soccer").subscriberCount(15L).build();
            Interest interest2 = Interest.builder().name("basketball").subscriberCount(100L).build();

            List<Interest> interests = Arrays.asList(interest1); // "soccer"만 검색되는 리스트
            List<InterestDto> interestDtos = Arrays.asList(
                InterestDto.builder().name("soccer").subscriberCount(150).build()
            );

            when(interestRepository.searchByKeywordOrName(eq(searchKeyword), eq(null), eq(limit + 1), eq(orderBy), eq(direction)))
                .thenReturn(interests);

            when(interestMapper.toDto(eq(interest1), eq(true))).thenReturn(interestDtos.get(0));

            // When
            CursorPageResponse<InterestDto> result = interestService.getInterests(searchKeyword, null, limit, orderBy, direction);

            // Then
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).name()).isEqualTo("soccer");
            assertThat(result.hasNext()).isFalse();

            verify(interestRepository).searchByKeywordOrName(eq(searchKeyword), eq(null), eq(limit + 1), eq(orderBy), eq(direction));
        }


        @Test
        void 관심사를_조회하면_이름으로_정렬한다() {
            // Given
            int limit = 10;
            String orderBy = "name";
            String direction = "asc";

            Interest interest1 = Interest.builder().name("aesthetic").subscriberCount(150L).build();
            Interest interest2 = Interest.builder().name("soccer").subscriberCount(200L).build();

            List<Interest> interests = Arrays.asList(interest1, interest2);
            List<InterestDto> interestDtos = Arrays.asList(
                InterestDto.builder().name("aesthetic").subscriberCount(150).build(),
                InterestDto.builder().name("soccer").subscriberCount(200).build()
            );

            when(interestRepository.searchByKeywordOrName(eq(null), eq(null), eq(limit + 1), eq(orderBy), eq(direction)))
                .thenReturn(interests);

            when(interestMapper.toDto(eq(interest1), eq(true))).thenReturn(interestDtos.get(0));
            when(interestMapper.toDto(eq(interest2), eq(true))).thenReturn(interestDtos.get(1));

            // When
            CursorPageResponse<InterestDto> result = interestService.getInterests(null, null, limit, orderBy, direction);

            // Then
            assertThat(result.content()).hasSize(2);
            assertThat(result.content().get(0).name()).isEqualTo("aesthetic");
            assertThat(result.content().get(1).name()).isEqualTo("soccer");
            assertThat(result.hasNext()).isFalse();

            verify(interestRepository).searchByKeywordOrName(eq(null), eq(null), eq(limit + 1), eq(orderBy), eq(direction));
        }

        @Test
        void 잘못된_정렬_기준_인경우_InvalidOrderParameterException이_발생한다() {
            // Given
            int limit = 10;
            String searchKeyword = "soccer";
            String orderBy = "invalidSort";
            String direction = "asc";

            // When
            Throwable throwable = catchThrowable(() -> interestService.getInterests(searchKeyword, null, limit, orderBy, direction));

            // Then
            assertThat(throwable).isInstanceOf(InvalidOrderParameterException.class)
                .hasMessageContaining("잘못된 정렬 기준입니다.");

            then(interestRepository).shouldHaveNoInteractions();
            then(interestMapper).shouldHaveNoInteractions();
        }

        @Test
        void 잘못된_페이지네이션_파라미터_인경우_InvalidPaginationException이_발생한다() {
            // Given
            int limit = 0;
            String orderBy = "name";
            String direction = "asc";

            // When
            Throwable throwable = catchThrowable(() -> interestService.getInterests(null, null, limit, orderBy, direction));

            // Then
            assertThat(throwable).isInstanceOf(InvalidPaginationException.class)
                .hasMessageContaining("데이터 limit값이 0입니다.");

            then(interestRepository).shouldHaveNoInteractions();
            then(interestMapper).shouldHaveNoInteractions();
        }
        @Test
        void 잘못된_cursor값_인경우_InterestCursorFormatException이_발생한다() {
            // Given
            int limit = 10;
            String searchKeyword = "soccer";
            String orderBy = "name";
            String direction = "asc";
            String cursor = "invalidCursorFormat";

            // When
            Throwable throwable = catchThrowable(() -> interestService.getInterests(searchKeyword, cursor, limit, orderBy, direction));

            // Then
            assertThat(throwable).isInstanceOf(InvalidCursorFormatException.class)
                .hasMessageContaining("cursor 값이 잘못되었습니다.");

            then(interestRepository).shouldHaveNoInteractions();
            then(interestMapper).shouldHaveNoInteractions();
        }
    }
}
