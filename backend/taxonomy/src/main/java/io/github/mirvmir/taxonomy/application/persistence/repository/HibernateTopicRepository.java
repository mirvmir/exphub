package io.github.mirvmir.taxonomy.application.persistence.repository;

import io.github.mirvmir.taxonomy.application.persistence.mapper.TopicMapper;
import io.github.mirvmir.taxonomy.application.service.port.repository.TopicRepository;
import io.github.mirvmir.taxonomy.domain.Topic;
import io.github.mirvmir.taxonomy.application.persistence.entity.TopicEntity;
import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
@AllArgsConstructor
public class HibernateTopicRepository implements TopicRepository {

    private final SessionFactory sessionFactory;
    private final TopicMapper topicMapper;

    @Override
    public Topic findById(Long id) {
        TopicEntity entity = sessionFactory.getCurrentSession()
                .get(TopicEntity.class, id);

        return topicMapper.toDomain(entity);
    }

    @Override
    public Topic findByIdAndSectionIdAndSubjectId(Long id,
                                                  Long sectionId,
                                                  Long subjectId) {
        TopicEntity entity = sessionFactory.getCurrentSession()
                .createQuery("""
                select t
                from TopicEntity t
                where t.id = :id
                  and t.sectionId = :sectionId
                  and t.subjectId = :subjectId
                """, TopicEntity.class)
                .setParameter("id", id)
                .setParameter("sectionId", sectionId)
                .setParameter("subjectId", subjectId)
                .uniqueResult();

        return topicMapper.toDomain(entity);
    }

    @Override
    public List<Topic> findAllBySectionId(Long sectionId) {
        List<TopicEntity> entities = sessionFactory.getCurrentSession()
                .createQuery("""
                select t
                from TopicEntity t
                where t.sectionId = :sectionId
                order by t.name
                """, TopicEntity.class)
                .setParameter("sectionId", sectionId)
                .getResultList();

        return topicMapper.toDomainList(entities);
    }

    @Override
    public List<Topic> findAllByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        List<TopicEntity> entities = sessionFactory.getCurrentSession()
                .createQuery("""
                select t
                from TopicEntity t
                where t.id in :ids
                """, TopicEntity.class)
                .setParameter("ids", ids)
                .getResultList();

        return topicMapper.toDomainList(entities);
    }

    @Override
    public Topic saveOrUpdate(Topic topic) {
        Session session = sessionFactory.getCurrentSession();

        TopicEntity entity = topicMapper.toEntity(topic);

        if (entity.getId() == null) {
            session.persist(entity);
        } else {
            entity = session.merge(entity);
        }

        return topicMapper.toDomain(entity);
    }
}