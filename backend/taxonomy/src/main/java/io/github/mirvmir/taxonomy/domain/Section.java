package io.github.mirvmir.taxonomy.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor(access = PRIVATE)
public class Section {
    private Long id;
    @NonNull
    private Long subjectId;
    @NonNull
    private String name;
    List<Topic> topics;

    public static Section create(Long subjectId,
                                    String name) {
        return new Section(
                null,
                subjectId,
                name,
                new ArrayList<>()
        );
    }

    public static Section load(Long id,
                               Long subjectId,
                               String name,
                               List<Topic> topics) {
        return new Section(
                id,
                subjectId,
                name,
                topics
        );
    }

    public void assignId(Long id) {
        this.id = id;
    }

    protected TopicSuggestion addTopicSuggestion(Long userId,
                                                 String title,
                                                 String description) {
        return TopicSuggestion.create(
                userId,
                this.id,
                this.subjectId,
                title,
                description
        );
    }
}
