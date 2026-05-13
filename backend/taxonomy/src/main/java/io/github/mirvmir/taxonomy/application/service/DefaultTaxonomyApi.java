package io.github.mirvmir.taxonomy.application.service;

import io.github.mirvmir.taxonomy.api.TaxonomyApi;
import io.github.mirvmir.taxonomy.api.dto.TopicTaxonomyInfoResponse;
import io.github.mirvmir.taxonomy.application.service.port.repository.TopicRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor
@Component
public class DefaultTaxonomyApi implements TaxonomyApi {

    private final TopicRepository topicRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TopicTaxonomyInfoResponse> getTopicTaxonomyInfo(Collection<Long> topicIds) {
        if (topicIds == null || topicIds.isEmpty()) {
            return List.of();
        }

        return topicRepository.findAllByIds(topicIds)
                .stream()
                .map(topic -> new TopicTaxonomyInfoResponse(
                        topic.getId(),
                        topic.getSectionId(),
                        topic.getSubjectId()
                ))
                .toList();
    }
}