package io.github.mirvmir.taxonomy.application.service.mapper;

import io.github.mirvmir.taxonomy.domain.Section;
import io.github.mirvmir.taxonomy.domain.Subject;
import io.github.mirvmir.taxonomy.domain.Topic;
import io.github.mirvmir.taxonomy.domain.TopicSuggestion;
import io.github.mirvmir.taxonomy.web.response.*;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TaxonomyResponseMapper {
    default SubjectResponse toSubjectResponse(Subject subject) {
        if (subject == null) {
            return null;
        }

        return new SubjectResponse(
                subject.getId(),
                subject.getName()
        );
    }

    default SubjectDetailsResponse toSubjectDetailsResponse(Subject subject) {
        if (subject == null) {
            return null;
        }

        List<SectionShortResponse> sections =
                subject.getSections()
                        .stream()
                        .map(this::toSectionShortResponse)
                        .toList();

        return new SubjectDetailsResponse(
                subject.getId(),
                subject.getName(),
                sections
        );
    }

    default SectionShortResponse toSectionShortResponse(Section section) {
        if (section == null) {
            return null;
        }

        List<TopicShortResponse> topics =
                section.getTopics()
                        .stream()
                        .map(this::toTopicShortResponse)
                        .toList();

        return new SectionShortResponse(
                section.getId(),
                section.getName(),
                topics
        );
    }

    default SectionDetailsResponse toSectionDetailsResponse(Section section) {
        if (section == null) {
            return null;
        }

        List<TopicShortResponse> topics =
                section.getTopics()
                        .stream()
                        .map(this::toTopicShortResponse)
                        .toList();

        return new SectionDetailsResponse(
                section.getId(),
                section.getSubjectId(),
                section.getName(),
                topics
        );
    }

    default TopicShortResponse toTopicShortResponse(Topic topic) {
        if (topic == null) {
            return null;
        }

        return new TopicShortResponse(
                topic.getId(),
                topic.getName()
        );
    }

    default TopicDetailsResponse toTopicDetailsResponse(Topic topic) {
        if (topic == null) {
            return null;
        }

        return new TopicDetailsResponse(
                topic.getId(),
                topic.getSubjectId(),
                topic.getSectionId(),
                topic.getName(),
                topic.getDescription()
        );
    }

    default TopicSuggestionResponse toTopicSuggestionResponse(TopicSuggestion suggestion) {
        if (suggestion == null) {
            return null;
        }

        return new TopicSuggestionResponse(
                suggestion.getId(),
                suggestion.getSubjectId(),
                suggestion.getSectionId(),
                suggestion.getName(),
                suggestion.getDescription(),
                suggestion.getStatus()
        );
    }
}