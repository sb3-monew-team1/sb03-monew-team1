package com.sprint.mission.sb03monewteam1.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.sb03monewteam1.dto.request.InterestRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.response.InterestResponse;
import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.exception.interest.InterestDuplicateException;
import com.sprint.mission.sb03monewteam1.fixture.InterestFixture;
import com.sprint.mission.sb03monewteam1.mapper.InterestMapper;
import com.sprint.mission.sb03monewteam1.repository.InterestRepository;

import org.junit.jupiter.api.DisplayName;
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

    @Test
    void 관심사를_등록하면_관심사_응답_DTO를_반환한다() {
        // Given
        InterestRegisterRequest request = InterestFixture.createInterestCreateRequest();
        Interest savedInterest = new Interest();
        InterestResponse expectedResponse = InterestFixture.createInterestResponseDto();

        given(interestRepository.existsByName(request.name())).willReturn(false);
        given(interestRepository.save(any(Interest.class))).willReturn(savedInterest);
        given(interestMapper.toDto(savedInterest, false)).willReturn(expectedResponse);

        // When
        InterestResponse result = interestService.create(request);

        // Then
        assertThat(result)
            .isNotNull()
            .extracting(InterestResponse::name)
            .isEqualTo(expectedResponse.name());

        then(interestRepository).should().existsByName(request.name());
        then(interestRepository).should().save(any(Interest.class));
        then(interestMapper).should().toDto(savedInterest, false);
    }

    @Test
    void 중복된_관심사_이름이면_InterestDuplicateException이_발생한다() {
        // Given
        InterestRegisterRequest request = InterestFixture.createInterestCreateRequest();
        given(interestRepository.existsByName(request.name())).willReturn(true);

        // When
        Throwable throwable = catchThrowable(() -> interestService.create(request));

        // Then
        assertThat(throwable).isInstanceOf(InterestDuplicateException.class);

        then(interestRepository).should().existsByName(request.name());
        then(interestRepository).shouldHaveNoMoreInteractions();
        then(interestMapper).shouldHaveNoInteractions();
    }
}
