package io.github.mirvmir.taxonomy.application.persistence.repository;

import io.github.mirvmir.taxonomy.application.persistence.mapper.TopicSuggestionMapper;
import io.github.mirvmir.taxonomy.application.service.port.repository.TopicSuggestionRepository;
import io.github.mirvmir.taxonomy.domain.SuggestionsStatus;
import io.github.mirvmir.taxonomy.domain.TopicSuggestion;
import io.github.mirvmir.taxonomy.application.persistence.entity.TopicSuggestionEntity;
import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@AllArgsConstructor
public class HibernateTopicSuggestionRepository implements TopicSuggestionRepository {

    private final SessionFactory sessionFactory;
    private final TopicSuggestionMapper topicSuggestionMapper;

    @Override
    public TopicSuggestion findById(Long id) {
        TopicSuggestionEntity entity = sessionFactory.getCurrentSession()
                .get(TopicSuggestionEntity.class, id);

        return topicSuggestionMapper.toDomain(entity);
    }

    @Override
    public List<TopicSuggestion> findAllByCreatedByUserId(Long userId) {
        List<TopicSuggestionEntity> entities = sessionFactory.getCurrentSession()
                .createQuery("""
                select ts
                from TopicSuggestionEntity ts
                where ts.createdByUserId = :userId
                order by ts.id desc
                """, TopicSuggestionEntity.class)
                .setParameter("userId", userId)
                .getResultList();

        return topicSuggestionMapper.toDomainList(entities);
    }

    @Override
    public List<TopicSuggestion> findAllByStatus(SuggestionsStatus status) {
        List<TopicSuggestionEntity> entities = sessionFactory.getCurrentSession()
                .createQuery("""
                select ts
                from TopicSuggestionEntity ts
                where ts.status = :status
                order by ts.id desc
                """, TopicSuggestionEntity.class)
                .setParameter("status", status)
                .getResultList();

        return topicSuggestionMapper.toDomainList(entities);
    }

    @Override
    public TopicSuggestion saveOrUpdate(TopicSuggestion suggestion) {
        Session session = sessionFactory.getCurrentSession();

        TopicSuggestionEntity entity = topicSuggestionMapper.toEntity(suggestion);

        if (entity.getId() == null) {
            session.persist(entity);
        } else {
            entity = session.merge(entity);
        }

        return topicSuggestionMapper.toDomain(entity);
    }
}