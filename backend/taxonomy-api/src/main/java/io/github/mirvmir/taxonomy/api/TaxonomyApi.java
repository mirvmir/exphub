package io.github.mirvmir.taxonomy.api;

import io.github.mirvmir.taxonomy.api.dto.TopicTaxonomyInfoResponse;

import java.util.Collection;
import java.util.List;

public interface TaxonomyApi {
    List<TopicTaxonomyInfoResponse> getTopicTaxonomyInfo(Collection<Long> topicIds);
}