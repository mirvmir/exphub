package io.github.mirvmir.taxonomy.application.persistence.repository;

import io.github.mirvmir.taxonomy.application.persistence.mapper.SubjectMapper;
import io.github.mirvmir.taxonomy.application.persistence.mapper.SectionMapper;
import io.github.mirvmir.taxonomy.application.persistence.mapper.TopicMapper;
import io.github.mirvmir.taxonomy.application.service.port.repository.SubjectRepository;
import io.github.mirvmir.taxonomy.domain.Section;
import io.github.mirvmir.taxonomy.domain.Subject;
import io.github.mirvmir.taxonomy.domain.Topic;
import io.github.mirvmir.taxonomy.application.persistence.entity.SectionEntity;
import io.github.mirvmir.taxonomy.application.persistence.entity.SubjectEntity;
import io.github.mirvmir.taxonomy.application.persistence.entity.TopicEntity;
import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@AllArgsConstructor
public class HibernateSubjectRepository implements SubjectRepository {

    private final SessionFactory sessionFactory;
    private final SubjectMapper subjectMapper;
    private final SectionMapper sectionMapper;
    private final TopicMapper topicMapper;

    @Override
    public Subject findById(Long id) {
        SubjectEntity entity = sessionFactory.getCurrentSession()
                .get(SubjectEntity.class, id);

        return subjectMapper.toDomainWithoutSections(entity);
    }

    @Override
    public Subject findByIdWithSectionsAndTopics(Long id) {
        Session session = sessionFactory.getCurrentSession();

        SubjectEntity subjectEntity = session.get(SubjectEntity.class, id);

        if (subjectEntity == null) {
            return null;
        }

        List<SectionEntity> sectionEntities = session.createQuery("""
                select s
                from SectionEntity s
                where s.subjectId = :subjectId
                order by s.name
                """, SectionEntity.class)
                .setParameter("subjectId", id)
                .getResultList();

        List<Section> sections = sectionEntities.stream()
                .map(sectionEntity -> {
                    List<TopicEntity> topicEntities = session.createQuery("""
                            select t
                            from TopicEntity t
                            where t.subjectId = :subjectId
                              and t.sectionId = :sectionId
                            order by t.name
                            """, TopicEntity.class)
                            .setParameter("subjectId", id)
                            .setParameter("sectionId", sectionEntity.getId())
                            .getResultList();

                    List<Topic> topics = topicMapper.toDomainList(topicEntities);

                    return sectionMapper.toDomain(sectionEntity, topics);
                })
                .toList();

        return subjectMapper.toDomain(subjectEntity, sections);
    }

    @Override
    public List<Subject> findAll() {
        List<SubjectEntity> entities = sessionFactory.getCurrentSession()
                .createQuery("""
                select s
                from SubjectEntity s
                order by s.name
                """, SubjectEntity.class)
                .getResultList();

        return entities.stream()
                .map(subjectMapper::toDomainWithoutSections)
                .toList();
    }

    @Override
    public Subject saveOrUpdate(Subject subject) {
        Session session = sessionFactory.getCurrentSession();

        SubjectEntity entity = subjectMapper.toEntity(subject);

        if (entity.getId() == null) {
            session.persist(entity);
        } else {
            entity = session.merge(entity);
        }

        return subjectMapper.toDomainWithoutSections(entity);
    }

    @Override
    public boolean existsByName(String name) {
        return Boolean.TRUE.equals(
                sessionFactory.getCurrentSession()
                        .createQuery("""
                                select count(s.id) > 0
                                from SubjectEntity s
                                where lower(s.name) = lower(:name)
                                """, Boolean.class)
                                .setParameter("name", name)
                                .uniqueResult()
        );
    }
}