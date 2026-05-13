package io.github.mirvmir.taxonomy.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor(access = PRIVATE)
public class Topic {
    private Long id;
    @NonNull
    private Long subjectId;
    @NonNull
    private Long sectionId;
    private String description;
    @NonNull
    private String name;

    public static Topic create(Long subjectId,
                                  Long sectionId,
                                  String description,
                                  String name) {
        return new Topic(
                null,
                subjectId,
                sectionId,
                description,
                name
        );
    }

    public static Topic load(Long id,
                             Long subjectId,
                             Long sectionId,
                             String description,
                             String name) {
        return new Topic(
                id,
                subjectId,
                sectionId,
                description,
                name
        );
    }

    public void assignId(Long id) {
        this.id = id;
    }
}
