package com.sprint.mission.sb03monewteam1.mapper;

import com.sprint.mission.sb03monewteam1.dto.SubscriptionActivityDto;
import com.sprint.mission.sb03monewteam1.entity.InterestKeyword;
import com.sprint.mission.sb03monewteam1.entity.Subscription;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface SubscriptionActivityMapper {

    @Mapping(target = "interestId", source = "interest.id")
    @Mapping(target = "interestName", source = "interest.name")
    @Mapping(target = "interestKeywords", source = "interest.keywords", qualifiedByName = "mapKeywords")
    @Mapping(target = "interestSubscriberCount", source = "interest.subscriberCount")
    SubscriptionActivityDto toDto(Subscription entity);

    @Named("mapKeywords")
    static List<String> mapKeywords(List<InterestKeyword> keywords) {
        return keywords.stream()
            .map(InterestKeyword::getKeyword)
            .toList();
    }
}
