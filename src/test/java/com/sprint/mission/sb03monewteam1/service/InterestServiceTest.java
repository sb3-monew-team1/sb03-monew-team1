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
import com.sprint.mission.sb03monewteam1.repository.InterestRepository;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponse;


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
        void 관심사를_조회하면_구독자수로_정렬한다() throws Exception {
            // Given
            int limit = 10;
            String sortBy = "subscriberCount";
            String sortDirection = "desc";

            Interest interest1 = InterestFixture.createInterest("aesthetic", Arrays.asList("경기", "스포츠"), 150);
            Interest interest2 = InterestFixture.createInterest("soccer", Arrays.asList("경기", "스포츠"), 200);

            List<Interest> interests = Arrays.asList(interest2, interest1); // sorting by subscriberCount descending
            List<InterestDto> interestDtos = Arrays.asList(
                InterestDto.builder().id(interest2.getId()).name("soccer").subscriberCount(200).build(),
                InterestDto.builder().id(interest1.getId()).name("aesthetic").subscriberCount(150).build()
            );

            when(interestRepository.searchByKeywordOrName(isNull(), isNull(), eq(limit), eq(sortBy), eq(sortDirection)))
                .thenReturn(interests);
            when(interestMapper.toDto(any(Interest.class), eq(true)))
                .thenReturn(interestDtos.get(0), interestDtos.get(1));

            // When
            CursorPageResponse<InterestDto> result = interestService.getInterests(null, null, limit, sortBy, sortDirection);

            // Then
            assertThat(result.content()).hasSize(2);
            assertThat(result.content().get(0).name()).isEqualTo("soccer");
            assertThat(result.content().get(1).name()).isEqualTo("aesthetic");
            assertThat(result.hasNext()).isFalse();

            verify(interestRepository).searchByKeywordOrName(isNull(), isNull(), eq(limit), eq(sortBy), eq(sortDirection));
        }

        @Test
        void 관심사를_조회하면_키워드로_검색한다() throws Exception {
            // Given
            int limit = 10;
            String sortBy = "name";
            String sortDirection = "asc";
            String searchKeyword = "스포츠";

            Interest interest1 = InterestFixture.createInterest("aesthetic", Arrays.asList("경기", "스포츠"), 150);
            Interest interest2 = InterestFixture.createInterest("soccer", Arrays.asList("경기", "스포츠"), 200);
            Interest interest3 = InterestFixture.createInterest("art", Arrays.asList("예술", "문화"), 50); // not matching the keyword

            List<Interest> interests = Arrays.asList(interest1, interest2);
            List<InterestDto> interestDtos = Arrays.asList(
                InterestDto.builder().id(interest1.getId()).name("aesthetic").subscriberCount(150).build(),
                InterestDto.builder().id(interest2.getId()).name("soccer").subscriberCount(200).build()
            );

            when(interestRepository.searchByKeywordOrName(eq(searchKeyword), isNull(), eq(limit), eq(sortBy), eq(sortDirection)))
                .thenReturn(interests);
            when(interestMapper.toDto(any(Interest.class), eq(true)))
                .thenReturn(interestDtos.get(0), interestDtos.get(1));

            // When
            CursorPageResponse<InterestDto> result = interestService.getInterests(searchKeyword, null, limit, sortBy, sortDirection);

            // Then
            assertThat(result.content()).hasSize(2);
            assertThat(result.content().get(0).name()).isEqualTo("aesthetic");
            assertThat(result.content().get(1).name()).isEqualTo("soccer");
            assertThat(result.hasNext()).isFalse();

            verify(interestRepository).searchByKeywordOrName(eq(searchKeyword), isNull(), eq(limit), eq(sortBy), eq(sortDirection));
        }

        @Test
        void 관심사를_조회하면_이름으로_정렬한다() throws Exception {
            // Given
            int limit = 10;
            String sortBy = "name";
            String sortDirection = "asc";

            Interest interest1 = InterestFixture.createInterest("aesthetic", Arrays.asList("경기", "스포츠"), 150);
            Interest interest2 = InterestFixture.createInterest("soccer", Arrays.asList("경기", "스포츠"), 200);

            List<Interest> interests = Arrays.asList(interest1, interest2);
            List<InterestDto> interestDtos = Arrays.asList(
                InterestDto.builder().id(interest1.getId()).name("aesthetic").subscriberCount(150).build(),
                InterestDto.builder().id(interest2.getId()).name("soccer").subscriberCount(200).build()
            );

            when(interestRepository.searchByKeywordOrName(isNull(), isNull(), eq(limit), eq(sortBy), eq(sortDirection)))
                .thenReturn(interests);
            when(interestMapper.toDto(any(Interest.class), eq(true)))
                .thenReturn(interestDtos.get(0), interestDtos.get(1));

            // When
            CursorPageResponse<InterestDto> result = interestService.getInterests(null, null, limit, sortBy, sortDirection);

            // Then
            assertThat(result.content()).hasSize(2);
            assertThat(result.content().get(0).name()).isEqualTo("aesthetic");
            assertThat(result.content().get(1).name()).isEqualTo("soccer");
            assertThat(result.hasNext()).isFalse();

            verify(interestRepository).searchByKeywordOrName(isNull(), isNull(), eq(limit), eq(sortBy), eq(sortDirection));
        }
    }
}
