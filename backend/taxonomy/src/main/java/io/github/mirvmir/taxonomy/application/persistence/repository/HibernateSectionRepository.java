package io.github.mirvmir.taxonomy.application.persistence.repository;

import io.github.mirvmir.taxonomy.application.persistence.mapper.SectionMapper;
import io.github.mirvmir.taxonomy.application.service.port.repository.SectionRepository;
import io.github.mirvmir.taxonomy.domain.Section;
import io.github.mirvmir.taxonomy.application.persistence.entity.SectionEntity;
import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@AllArgsConstructor
public class HibernateSectionRepository implements SectionRepository {

    private final SessionFactory sessionFactory;
    private final SectionMapper sectionMapper;

    @Override
    public Section findById(Long id) {
        SectionEntity entity = sessionFactory.getCurrentSession()
                .get(SectionEntity.class, id);

        return sectionMapper.toDomainWithoutTopics(entity);
    }

    @Override
    public Section findByIdAndSubjectId(Long id, Long subjectId) {
        SectionEntity entity = sessionFactory.getCurrentSession()
                .createQuery("""
                select s
                from SectionEntity s
                where s.id = :id
                  and s.subjectId = :subjectId
                """, SectionEntity.class)
                .setParameter("id", id)
                .setParameter("subjectId", subjectId)
                .uniqueResult();

        return sectionMapper.toDomainWithoutTopics(entity);
    }

    @Override
    public List<Section> findAllBySubjectId(Long subjectId) {
        List<SectionEntity> entities = sessionFactory.getCurrentSession()
                .createQuery("""
                select s
                from SectionEntity s
                where s.subjectId = :subjectId
                order by s.name
                """, SectionEntity.class)
                .setParameter("subjectId", subjectId)
                .getResultList();

        return entities.stream()
                .map(sectionMapper::toDomainWithoutTopics)
                .toList();
    }

    @Override
    public Section saveOrUpdate(Section section) {
        Session session = sessionFactory.getCurrentSession();

        SectionEntity entity = sectionMapper.toEntity(section);

        if (entity.getId() == null) {
            session.persist(entity);
        } else {
            entity = session.merge(entity);
        }

        return sectionMapper.toDomainWithoutTopics(entity);
    }

    @Override
    public boolean existsBySubjectIdAndName(Long subjectId, String name) {
        return Boolean.TRUE.equals(
                sessionFactory.getCurrentSession()
                        .createQuery("""
                                select count(s.id) > 0
                                from SectionEntity s
                                where s.subjectId = :subjectId
                                  and lower(s.name) = lower(:name)
                                """, Boolean.class)
                                .setParameter("subjectId", subjectId)
                                .setParameter("name", name)
                                .uniqueResult()
        );
    }
}