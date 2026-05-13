package io.github.mirvmir.taxonomy.application.service.port.repository;

import io.github.mirvmir.taxonomy.domain.Section;

import java.util.List;

public interface SectionRepository {
    Section findById(Long id);
    Section findByIdAndSubjectId(Long id, Long subjectId);
    List<Section> findAllBySubjectId(Long subjectId);
    Section saveOrUpdate(Section section);
    boolean existsBySubjectIdAndName(Long subjectId, String name);
}