package io.github.mirvmir.taxonomy.application.service.port.repository;

import io.github.mirvmir.taxonomy.domain.Topic;

import java.util.Collection;
import java.util.List;

public interface TopicRepository {
    Topic findById(Long id);
    Topic findByIdAndSectionIdAndSubjectId(Long id,
                                           Long sectionId,
                                           Long subjectId);
    List<Topic> findAllBySectionId(Long sectionId);
    List<Topic> findAllByIds(Collection<Long> ids);
    Topic saveOrUpdate(Topic topic);
    boolean existsBySectionIdAndName(Long sectionId,
                                     String name);
}
