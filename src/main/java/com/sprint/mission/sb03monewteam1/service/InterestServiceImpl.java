package com.sprint.mission.sb03monewteam1.service;

import com.sprint.mission.sb03monewteam1.dto.request.InterestRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.response.InterestResponse;
import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.entity.InterestKeyword;
import com.sprint.mission.sb03monewteam1.exception.interest.InterestDuplicateException;
import com.sprint.mission.sb03monewteam1.exception.interest.InterestSimilarityException;
import com.sprint.mission.sb03monewteam1.mapper.InterestMapper;
import com.sprint.mission.sb03monewteam1.repository.InterestRepository;
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
    public InterestResponse create(InterestRegisterRequest request) {
        log.info("새로운 관심사 등록 요청: {}", request);

        if (interestRepository.existsByName(request.name())) {
            log.warn("중복된 관심사 이름: {}", request.name());
            throw new InterestDuplicateException(request.name());
        }

        if (isSimilarityAboveThreshold(request.name())) {
            log.warn("유사한 관심사 이름: {}", request.name());
            throw new InterestSimilarityException("request.name()");
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

        InterestResponse response = interestMapper.toDto(saved, false);
        log.info("관심사 등록 완료: {}", response);

        return response;
    }

    private boolean isSimilarityAboveThreshold(String newInterestName) {
        for (Interest existingInterest : interestRepository.findAll()) {
            double similarity = calculateSimilarity(existingInterest.getName(), newInterestName);
            if (similarity >= 0.8) {
                return true;
            }
        }
        return false;
    }

    private double calculateSimilarity(String name1, String name2) {
        LevenshteinDistance levenshtein = LevenshteinDistance.getDefaultInstance();
        int distance = levenshtein.apply(name1, name2);
        int maxLength = Math.max(name1.length(), name2.length());
        return 1.0 - ((double) distance / maxLength);
    }
}
