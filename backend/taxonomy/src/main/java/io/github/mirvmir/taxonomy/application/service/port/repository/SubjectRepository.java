package io.github.mirvmir.taxonomy.application.service.port.repository;

import io.github.mirvmir.taxonomy.domain.Subject;

import java.util.List;

public interface SubjectRepository {
    Subject findById(Long id);
    Subject findByIdWithSectionsAndTopics(Long id);
    List<Subject> findAll();
    Subject saveOrUpdate(Subject subject);
    boolean existsByName(String name);
}
