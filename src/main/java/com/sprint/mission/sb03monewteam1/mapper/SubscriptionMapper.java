package com.sprint.mission.sb03monewteam1.mapper;

import com.sprint.mission.sb03monewteam1.dto.SubscriptionDto;
import com.sprint.mission.sb03monewteam1.entity.InterestKeyword;
import com.sprint.mission.sb03monewteam1.entity.Subscription;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    @Mappings({
        @Mapping(source = "interest.id", target = "interestId"),
        @Mapping(source = "interest.name", target = "interestName"),
        @Mapping(source = "interest.keywords", target = "interestKeywords"),
        @Mapping(source = "interest.subscriberCount", target = "interestSubscriberCount")
    })
    SubscriptionDto toDto(Subscription subscription);

    default List<String> map(List<InterestKeyword> keywords) {
        if (keywords == null) {
            return null;
        }
        return keywords.stream()
            .map(InterestKeyword::getKeyword)
            .collect(java.util.stream.Collectors.toList());
    }
}
