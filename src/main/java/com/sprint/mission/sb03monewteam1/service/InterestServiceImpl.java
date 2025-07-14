package com.sprint.mission.sb03monewteam1.service;

import com.sprint.mission.sb03monewteam1.dto.request.InterestRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.InterestDto;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponse;
import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.entity.InterestKeyword;
import com.sprint.mission.sb03monewteam1.exception.interest.InterestDuplicateException;
import com.sprint.mission.sb03monewteam1.exception.interest.InterestSimilarityException;
import com.sprint.mission.sb03monewteam1.mapper.InterestMapper;
import com.sprint.mission.sb03monewteam1.repository.InterestRepository;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InterestServiceImpl implements InterestService {

    private final InterestRepository interestRepository;
    private final InterestMapper interestMapper;

    @Override
    public InterestDto create(InterestRegisterRequest request) {
        log.info("새로운 관심사 등록 요청: {}", request);

        if (interestRepository.existsByName(request.name())) {
            log.warn("중복된 관심사 이름: {}", request.name());
            throw new InterestDuplicateException(request.name());
        }

        String similarInterestName = findSimilarInterestName(request.name());
        if (similarInterestName != null) {
            log.warn("유사한 관심사 이름: {}", similarInterestName);
            throw new InterestSimilarityException(similarInterestName);
        }

        Interest interest = Interest.builder()
            .name(request.name())
            .build();

        for (String keyword : request.keywords()) {
            InterestKeyword interestKeyword = InterestKeyword.builder()
                .interest(interest)
                .keyword(keyword)
                .build();
            interest.getKeywords().add(interestKeyword);
        }

        Interest saved = interestRepository.save(interest);

        InterestDto response = interestMapper.toDto(saved, false);
        log.info("관심사 등록 완료: {}", response);

        return response;
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<InterestDto> getInterests(
        String searchKeyword, String cursor, int limit, String sortBy, String sortDirection) {

        List<Interest> interests = interestRepository.searchByKeywordOrName(
            searchKeyword, cursor, limit, sortBy, sortDirection);

        List<InterestDto> content = interests.stream()
            .map(interest -> interestMapper.toDto(interest, true))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        String nextCursor = calculateNextCursor(interests, limit);
        Instant nextAfter = calculateNextAfter(interests);

        boolean hasNext = interests.size() > limit;
        long totalElements = interestRepository.count();

        return new CursorPageResponse<InterestDto>(content, nextCursor, nextAfter, limit, totalElements, hasNext);
    }

    private String calculateNextCursor(List<Interest> interests, int limit) {
        if (interests.size() > limit) {
            return String.valueOf(interests.get(limit).getId());
        }
        return null;
    }

    private Instant calculateNextAfter(List<Interest> interests) {
        if (!interests.isEmpty()) {
            return interests.get(interests.size() - 1).getUpdatedAt();
        }
        return null;
    }
    private String findSimilarInterestName(String newInterestName) {
        for (Interest existingInterest : interestRepository.findAll()) {
            double similarity = calculateSimilarity(existingInterest.getName(), newInterestName);
            if (similarity >= 0.8) {
                return existingInterest.getName();
            }
        }
        return null;
    }

    private double calculateSimilarity(String name1, String name2) {
        LevenshteinDistance levenshtein = LevenshteinDistance.getDefaultInstance();
        int distance = levenshtein.apply(name1, name2);
        int maxLength = Math.max(name1.length(), name2.length());
        return 1.0 - ((double) distance / maxLength);
    }
}
