package io.github.mirvmir.taxonomy.application.service.port.repository;

import io.github.mirvmir.taxonomy.domain.SuggestionsStatus;
import io.github.mirvmir.taxonomy.domain.TopicSuggestion;

import java.util.List;

public interface TopicSuggestionRepository {
    TopicSuggestion findById(Long id);
    List<TopicSuggestion> findAllByCreatedByUserId(Long userId);
    List<TopicSuggestion> findAllByStatus(SuggestionsStatus status);
    TopicSuggestion saveOrUpdate(TopicSuggestion suggestion);
}