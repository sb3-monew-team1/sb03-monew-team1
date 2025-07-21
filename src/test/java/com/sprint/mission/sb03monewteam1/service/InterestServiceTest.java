package com.sprint.mission.sb03monewteam1.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.sprint.mission.sb03monewteam1.dto.InterestDto;
import com.sprint.mission.sb03monewteam1.dto.SubscriptionDto;
import com.sprint.mission.sb03monewteam1.dto.request.InterestRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.request.InterestUpdateRequest;
import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.entity.InterestKeyword;
import com.sprint.mission.sb03monewteam1.entity.Subscription;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.event.SubscriptionActivityCreateEvent;
import com.sprint.mission.sb03monewteam1.exception.common.InvalidCursorException;
import com.sprint.mission.sb03monewteam1.exception.common.InvalidSortOptionException;
import com.sprint.mission.sb03monewteam1.exception.interest.InterestDuplicateException;
import com.sprint.mission.sb03monewteam1.exception.interest.InterestNotFoundException;
import com.sprint.mission.sb03monewteam1.exception.interest.InterestSimilarityException;
import com.sprint.mission.sb03monewteam1.exception.interest.SubscriptionNotFoundException;
import com.sprint.mission.sb03monewteam1.fixture.InterestFixture;
import com.sprint.mission.sb03monewteam1.fixture.UserFixture;
import com.sprint.mission.sb03monewteam1.mapper.InterestMapper;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponse;


import com.sprint.mission.sb03monewteam1.mapper.SubscriptionMapper;
import com.sprint.mission.sb03monewteam1.repository.jpa.interest.InterestKeywordRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.interest.InterestRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.subscription.SubscriptionRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.user.UserRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("InterestService 테스트")
class InterestServiceTest {

    @Mock
    private InterestRepository interestRepository;

    @Mock
    private InterestKeywordRepository interestKeywordRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private InterestMapper interestMapper;

    @Mock
    private SubscriptionMapper subscriptionMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private InterestServiceImpl interestService;

    @Nested
    @DisplayName("관심사 생성 테스트")
    class InterestCreateTests {

        @Test
        void 관심사를_등록하면_관심사_응답_DTO를_반환한다() {

            // Given
            InterestRegisterRequest request = InterestFixture.createInterestRegisterRequest();
            Interest savedInterest = InterestFixture.createInterest();
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

            Interest existingInterest = InterestFixture.createInterest();
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

            User user = User.builder()
                .nickname("testUser")
                .build();

            Interest interest1 = Interest.builder().name("aesthetic").subscriberCount(150L).build();
            Interest interest2 = Interest.builder().name("soccer").subscriberCount(200L).build();

            List<Interest> interests = Arrays.asList(interest2, interest1);

            when(
                interestRepository.searchByKeywordOrName(null, null, limit + 1, orderBy, direction))
                .thenReturn(interests);

            Subscription subscription1 = Subscription.builder()
                .user(user)
                .interest(interest1)
                .build();

            Subscription subscription2 = Subscription.builder()
                .user(user)
                .interest(interest2)
                .build();

            when(subscriptionRepository.findAllByUserId(user.getId()))
                .thenReturn(Arrays.asList(subscription1, subscription2));

            // When
            CursorPageResponse<InterestDto> result = interestService.getInterests(user.getId(),
                null, null, limit, orderBy, direction);

            // Then
            assertThat(result.content()).hasSize(2);
            assertThat(result.content().get(0).name()).isEqualTo("soccer");
            assertThat(result.content().get(1).name()).isEqualTo("aesthetic");
            assertThat(result.hasNext()).isFalse();

            verify(interestRepository).searchByKeywordOrName(null, null, limit + 1, orderBy,
                direction);
            verify(subscriptionRepository).findAllByUserId(user.getId());
        }

        @Test
        void 관심사를_조회하면_키워드로_검색한다() {
            // Given
            int limit = 10;
            String keyword = "soccer";
            String orderBy = "name";
            String direction = "asc";

            Interest interest1 = Interest.builder().name("soccer").subscriberCount(15L).build();
            Interest interest2 = Interest.builder().name("basketball").subscriberCount(100L)
                .build();

            List<Interest> interests = Arrays.asList(interest1);
            List<InterestDto> interestDtos = Arrays.asList(
                InterestDto.builder().name("soccer").subscriberCount(150L).build()
            );

            when(interestRepository.searchByKeywordOrName(eq(keyword), eq(null), eq(limit + 1),
                eq(orderBy), eq(direction)))
                .thenReturn(interests);

            // When
            CursorPageResponse<InterestDto> result = interestService.getInterests(UUID.randomUUID(),
                keyword, null, limit, orderBy, direction);

            // Then
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).name()).isEqualTo("soccer");
            assertThat(result.hasNext()).isFalse();

            verify(interestRepository).searchByKeywordOrName(eq(keyword), eq(null), eq(limit + 1),
                eq(orderBy), eq(direction));
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
                InterestDto.builder().name("aesthetic").subscriberCount(150L).build(),
                InterestDto.builder().name("soccer").subscriberCount(200L).build()
            );

            when(interestRepository.searchByKeywordOrName(eq(null), eq(null), eq(limit + 1),
                eq(orderBy), eq(direction)))
                .thenReturn(interests);

            // When
            CursorPageResponse<InterestDto> result = interestService.getInterests(UUID.randomUUID(),
                null, null, limit, orderBy, direction);

            // Then
            assertThat(result.content()).hasSize(2);
            assertThat(result.content().get(0).name()).isEqualTo("aesthetic");
            assertThat(result.content().get(1).name()).isEqualTo("soccer");
            assertThat(result.hasNext()).isFalse();

            verify(interestRepository).searchByKeywordOrName(eq(null), eq(null), eq(limit + 1),
                eq(orderBy), eq(direction));
        }

        @Test
        void 잘못된_정렬_기준_인경우_InvalidSortOptionException이_발생한다() {
            // Given
            int limit = 10;
            String keyword = "soccer";
            String orderBy = "invalidSort";
            String direction = "asc";

            // When
            Throwable throwable = catchThrowable(
                () -> interestService.getInterests(UUID.randomUUID(), keyword, null, limit, orderBy,
                    direction));

            // Then
            assertThat(throwable).isInstanceOf(InvalidSortOptionException.class)
                .hasMessageContaining("지원하지 않는 정렬 필드입니다.");

            then(interestRepository).shouldHaveNoInteractions();
            then(interestMapper).shouldHaveNoInteractions();
        }

        @Test
        void 잘못된_cursor값_인경우_InvalidCursorException이_발생한다() {
            // Given
            int limit = 10;
            String keyword = "soccer";
            String orderBy = "subscriberCount";
            String direction = "asc";
            String cursor = "invalidCursorFormat";

            // When
            Throwable throwable = catchThrowable(
                () -> interestService.getInterests(UUID.randomUUID(), keyword, cursor, limit,
                    orderBy, direction));

            // Then
            assertThat(throwable).isInstanceOf(InvalidCursorException.class)
                .hasMessageContaining("잘못된 커서 형식입니다.");

            then(interestRepository).shouldHaveNoInteractions();
            then(interestMapper).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("관심사 구독 테스트")
    class InterestSubscribeTests {

        @Test
        void 관심사를_구독하면_구독된_관심사_응답_DTO를_반환한다() {
            // Given
            Interest interest = Interest.builder()
                .name("aesthetic")
                .subscriberCount(150L)
                .build();

            User user = User.builder()
                .nickname("testUser")
                .build();

            Subscription subscription = new Subscription(interest, user);

            SubscriptionDto expectedDto = SubscriptionDto.builder()
                .id(subscription.getId())
                .interestId(interest.getId())
                .interestName(interest.getName())
                .interestSubscriberCount(interest.getSubscriberCount())
                .createdAt(subscription.getCreatedAt())
                .build();

            when(interestRepository.findById(interest.getId())).thenReturn(Optional.of(interest));
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);
            when(subscriptionMapper.toDto(subscription)).thenReturn(expectedDto);

            // When
            SubscriptionDto result = interestService.createSubscription(interest.getId(),
                user.getId());

            // Then
            assertThat(result).isNotNull();
            assertThat(interest.getSubscriberCount()).isEqualTo(151L);
            assertThat(result.interestId()).isEqualTo(expectedDto.interestId());
            assertThat(result.interestName()).isEqualTo(expectedDto.interestName());
            assertThat(result.interestSubscriberCount()).isEqualTo(
                expectedDto.interestSubscriberCount());
            assertThat(result.createdAt()).isEqualTo(expectedDto.createdAt());
            verify(interestRepository).findById(interest.getId());
            verify(eventPublisher).publishEvent(any(SubscriptionActivityCreateEvent.class));
        }


        @Test
        void 구독하려는_관심사가_없는_경우_InterestNotFoundException가_발생한다() {
            // Given
            UUID nonExistentInterestId = UUID.randomUUID();
            User user = User.builder()
                .nickname("testUser")
                .build();

            when(interestRepository.findById(nonExistentInterestId)).thenReturn(Optional.empty());

            // When & Then
            InterestNotFoundException exception = assertThrows(InterestNotFoundException.class,
                () -> {
                    interestService.createSubscription(user.getId(), nonExistentInterestId);
                });

            assertThat(exception).hasMessageContaining("관심사를 찾을 수 없습니다.");

            verify(interestRepository).findById(nonExistentInterestId);
        }
    }

    @Nested
    @DisplayName("관심사 키워드 수정 테스트")
    class InterestUpdateKeywordsTests {

        @Test
        void 관심사_키워드를_수정하면_수정된_관심사_응답_DTO를_반환한다() {
            // Given
            UUID interestId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            List<String> newKeywords = Arrays.asList("keyword1", "keyword2");

            Interest existingInterest = Interest.builder()
                .name("Interest 1")
                .subscriberCount(10L)
                .build();
            existingInterest.setKeywords(new ArrayList<>());

            Interest updatedInterest = Interest.builder()
                .name("Interest 1")
                .subscriberCount(10L)
                .build();
            updatedInterest.setKeywords(Arrays.asList(
                InterestKeyword.builder().keyword("keyword1").interest(updatedInterest).build(),
                InterestKeyword.builder().keyword("keyword2").interest(updatedInterest).build()
            ));

            InterestDto expectedResponse = InterestDto.builder()
                .id(interestId)
                .name("Interest 1")
                .keywords(Arrays.asList("keyword1", "keyword2"))
                .subscribedByMe(true) // 예시로 구독된 상태로 설정
                .build();

            InterestUpdateRequest request = new InterestUpdateRequest(newKeywords);

            given(interestRepository.findById(interestId)).willReturn(
                Optional.of(existingInterest));
            given(
                subscriptionRepository.existsByUserIdAndInterestId(userId, interestId)).willReturn(
                true);
            given(interestRepository.save(existingInterest)).willReturn(updatedInterest);
            given(interestMapper.toDto(updatedInterest, true)).willReturn(expectedResponse);

            // When
            InterestDto result = interestService.updateInterestKeywords(interestId, request,
                userId);

            // Then
            assertThat(result)
                .isNotNull()
                .extracting(InterestDto::name)
                .isEqualTo(expectedResponse.name());
            assertThat(result.keywords()).containsExactly("keyword1", "keyword2");
            assertThat(result.subscribedByMe()).isTrue();

            verify(interestRepository).findById(interestId);
            verify(interestRepository).save(existingInterest);
            verify(interestMapper).toDto(updatedInterest, true);
        }


        @Test
        void 존재하지_않는_관심사를_수정하려_하면_InterestNotFoundException이_발생한다() {
            // Given
            UUID nonExistentInterestId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            List<String> newKeywords = Arrays.asList("keyword1", "keyword2");

            InterestUpdateRequest request = new InterestUpdateRequest(newKeywords);

            given(interestRepository.findById(nonExistentInterestId)).willReturn(Optional.empty());

            // When & Then
            InterestNotFoundException exception = assertThrows(InterestNotFoundException.class,
                () -> {
                    interestService.updateInterestKeywords(nonExistentInterestId, request, userId);
                });

            assertThat(exception).hasMessageContaining("관심사를 찾을 수 없습니다.");

            verify(interestRepository).findById(nonExistentInterestId);
        }
    }

    @Nested
    @DisplayName("관심사 삭제 테스트")
    class InterestDeleteTests {

        @Test
        void 관심사를_삭제하면_삭제가_성공한다() {
            // Given
            Interest interest = InterestFixture.createInterest();

            when(interestRepository.findById(interest.getId())).thenReturn(Optional.of(interest));

            // When
            interestService.deleteInterest(interest.getId());

            // Then
            verify(interestRepository).findById(interest.getId());
            verify(interestRepository).delete(interest);
            verify(interestKeywordRepository).deleteByInterestId(interest.getId());
            verify(subscriptionRepository).deleteByInterestId(interest.getId());
        }

        @Test
        void 존재하지_않는_관심사를_삭제하면_InterestNotFoundException이_발생한다() {
            // Given
            UUID nonExistentInterestId = UUID.randomUUID();
            when(interestRepository.findById(nonExistentInterestId)).thenReturn(Optional.empty());

            // When & Then
            InterestNotFoundException exception = assertThrows(InterestNotFoundException.class,
                () -> {
                    interestService.deleteInterest(nonExistentInterestId);
                });

            assertThat(exception).hasMessageContaining("관심사를 찾을 수 없습니다.");

            verify(interestRepository).findById(nonExistentInterestId);
            verify(interestRepository, times(0)).delete(any());
        }
    }

    @Nested
    @DisplayName("관심사 구독 취소 테스트")
    class DeleteSubscriptionTests {

        @Test
        void 관심사_구독을_취소하면_구독이_삭제된다() {
            // Given
            Interest interest = InterestFixture.createInterest();
            User user = UserFixture.createUser();
            Subscription subscription = new Subscription(interest, user);
            long originalCount = interest.getSubscriberCount();

            when(interestRepository.findById(interest.getId()))
                .thenReturn(Optional.of(interest));
            when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
            when(subscriptionRepository.findByUserIdAndInterestId(user.getId(), interest.getId()))
                .thenReturn(Optional.of(subscription));

            // When
            interestService.deleteSubscription(user.getId(), interest.getId());

            // Then
            verify(subscriptionRepository).delete(subscription);
            assertThat(interest.getSubscriberCount()).isEqualTo(originalCount - 1);
            verify(interestRepository).findById(interest.getId());
        }

        @Test
        void 존재하지_않는_관심사를_구독취소하면_예외가_발생한다() {
            // Given
            UUID nonExistentInterestId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            when(interestRepository.findById(nonExistentInterestId))
                .thenReturn(Optional.empty());

            // When & Then
            InterestNotFoundException exception = assertThrows(
                InterestNotFoundException.class,
                () -> interestService.deleteSubscription(userId, nonExistentInterestId)
            );

            assertThat(exception)
                .hasMessageContaining("관심사를 찾을 수 없습니다");

            verify(interestRepository).findById(nonExistentInterestId);
            verifyNoMoreInteractions(subscriptionRepository, userRepository);
        }

        @Test
        void 구독정보가_존재하지_않으면_예외가_발생한다() {
            // Given
            UUID userId = UUID.randomUUID();
            UUID interestId = UUID.randomUUID();

            Interest interest = InterestFixture.createInterest();
            User user = UserFixture.createUser();

            when(interestRepository.findById(interestId)).thenReturn(Optional.of(interest));
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(subscriptionRepository.findByUserIdAndInterestId(userId, interestId)).thenReturn(
                Optional.empty());

            // When & Then
            SubscriptionNotFoundException exception = assertThrows(
                SubscriptionNotFoundException.class,
                () -> interestService.deleteSubscription(userId, interestId)
            );

            assertThat(exception)
                .hasMessageContaining("구독 정보를 찾을 수 없습니다");

            verify(interestRepository).findById(interestId);
            verify(userRepository).findById(userId);
            verify(subscriptionRepository).findByUserIdAndInterestId(userId, interestId);
            verifyNoMoreInteractions(subscriptionRepository);
        }
    }
}
