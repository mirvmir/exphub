package io.github.mirvmir.taxonomy.domain;

import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.taxonomy.exception.TaxonomyErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor(access = PRIVATE)
public class Subject {
    private Long id;
    @NonNull
    private String name;
    List<Section> sections;

    public static Subject create(String name) {
        return new Subject(
                null,
                name,
                new ArrayList<>()
        );
    }

    public static Subject load(Long id,
                               String name,
                               List<Section> sections) {
        return new Subject(
                id,
                name,
                sections
        );
    }

    public List<Section> getSections() {
        return List.copyOf(this.sections);
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public Section addSection(String title) {
        Section newSection = Section.create(
                this.id,
                title
        );
        this.sections.add(newSection);
        return newSection;
    }

    public TopicSuggestion addTopicSuggestion(Long userId,
                                              Long sectionId,
                                              String title,
                                              String description) {
        Section section = findSection(sectionId);
        return section.addTopicSuggestion(
                userId,
                title,
                description
        );
    }

    private Section findSection(Long sectionId) {
        return sections.stream()
                .filter(m -> m.getId().equals(sectionId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(TaxonomyErrorCode.SECTION_NOT_FOUND));
    }

    private Topic findTopic(Long sectionId, Long topicId) {
        return sections.stream()
                .filter(m -> m.getId().equals(sectionId))
                .flatMap(m -> m.getTopics().stream())
                .filter(l -> l.getId().equals(topicId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(TaxonomyErrorCode.TOPIC_NOT_FOUND));
    }
}
