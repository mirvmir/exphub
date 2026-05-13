package io.github.mirvmir.catalog.application.persistence.repository;

import io.github.mirvmir.catalog.application.service.port.repository.ActivityCatalogRepository;
import io.github.mirvmir.catalog.domain.ActivityCatalog;
import io.github.mirvmir.catalog.application.service.dto.CatalogFilterDto;
import io.github.mirvmir.catalog.application.persistence.entity.ActivityCatalogEntity;
import io.github.mirvmir.catalog.application.persistence.mapper.ActivityCatalogMapper;
import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
@Repository
public class HibernateActivityCatalogRepository implements ActivityCatalogRepository {

    private final SessionFactory sessionFactory;
    private final ActivityCatalogMapper activityCatalogMapper;

    @Override
    public List<ActivityCatalog> search(CatalogFilterDto filter) {
        Session session = sessionFactory.getCurrentSession();

        String sql = """
                select distinct a.*
                from activity_catalog a
                where (:search is null
                       or lower(a.title) like :search
                       or lower(a.author_name) like :search
                       or lower(a.short_description) like :search)
                  and (:minPrice is null or a.amount >= :minPrice)
                  and (:maxPrice is null or a.amount <= :maxPrice)
                  and (:minRating is null or a.rating_avg >= :minRating)
                  and (:format is null or a.format = :format)
                  and (:topicId is null or exists (
                        select 1
                        from activity_catalog_topic act
                        where act.activity_catalog_id = a.id
                          and act.topic_id = :topicId
                  ))
                  and (:sectionId is null or exists (
                        select 1
                        from activity_catalog_section acs
                        where acs.activity_catalog_id = a.id
                          and acs.section_id = :sectionId
                  ))
                  and (:subjectId is null or exists (
                        select 1
                        from activity_catalog_subject acsu
                        where acsu.activity_catalog_id = a.id
                          and acsu.subject_id = :subjectId
                  ))
                order by a.rating_avg desc nulls last
                """;

        List<ActivityCatalogEntity> entities = session
                .createNativeQuery(sql, ActivityCatalogEntity.class)
                .setParameter("search", prepareSearch(filter.search()))
                .setParameter("minPrice", filter.minPrice())
                .setParameter("maxPrice", filter.maxPrice())
                .setParameter("minRating", filter.minRating())
                .setParameter("format", filter.format() == null ? null : filter.format().name())
                .setParameter("topicId", filter.topicId())
                .setParameter("sectionId", filter.sectionId())
                .setParameter("subjectId", filter.subjectId())
                .getResultList();

        return entities.stream()
                .map(activityCatalogMapper::toDomain)
                .toList();
    }

    @Override
    public void saveOrUpdate(ActivityCatalog activityCatalog) {
        Session session = sessionFactory.getCurrentSession();

        ActivityCatalogEntity existingEntity = session.createQuery("""
                select a
                from ActivityCatalogEntity a
                where a.activityId = :activityId
                """, ActivityCatalogEntity.class)
                .setParameter("activityId", activityCatalog.getActivityId())
                .setMaxResults(1)
                .uniqueResult();

        ActivityCatalogEntity entity =
                activityCatalogMapper.toEntity(activityCatalog);

        if (existingEntity == null) {
            session.persist(entity);
            return;
        }

        session.merge(entity);
    }

    @Override
    public void deleteByActivityId(Long activityId) {
        Session session = sessionFactory.getCurrentSession();

        session.createMutationQuery("""
                delete from ActivityCatalogEntity a
                where a.activityId = :activityId
                """)
                .setParameter("activityId", activityId)
                .executeUpdate();
    }

    @Override
    public ActivityCatalog findByActivityId(Long activityId) {
        Session session = sessionFactory.getCurrentSession();

        ActivityCatalogEntity entity = session.createQuery("""
                select a
                from ActivityCatalogEntity a
                where a.activityId = :activityId
                """, ActivityCatalogEntity.class)
                .setParameter("activityId", activityId)
                .setMaxResults(1)
                .uniqueResult();

        return activityCatalogMapper.toDomain(entity);
    }

    @Override
    public void updateTopicIds(Long activityId, Set<Long> topicIds) {
        Session session = sessionFactory.getCurrentSession();

        session.createMutationQuery("""
                update ActivityCatalogEntity c
                set c.topicIds = :topicIds
                where c.activityId = :activityId
                """)
                .setParameter("activityId", activityId)
                .setParameter("topicIds", topicIds)
                .executeUpdate();
    }

    @Override
    public void updateTaxonomyIds(Long activityId,
                                  Set<Long> topicIds,
                                  Set<Long> sectionIds,
                                  Set<Long> subjectIds) {
        Session session = sessionFactory.getCurrentSession();

        ActivityCatalogEntity entity = session.createQuery("""
                select a
                from ActivityCatalogEntity a
                where a.activityId = :activityId
                """, ActivityCatalogEntity.class)
                .setParameter("activityId", activityId)
                .setMaxResults(1)
                .uniqueResult();

        if (entity == null) {
            return;
        }

        entity.getTopicIds().clear();
        entity.getTopicIds().addAll(topicIds == null ? Set.of() : topicIds);

        entity.getSectionIds().clear();
        entity.getSectionIds().addAll(sectionIds == null ? Set.of() : sectionIds);

        entity.getSubjectIds().clear();
        entity.getSubjectIds().addAll(subjectIds == null ? Set.of() : subjectIds);
    }

    private String prepareSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }

        return "%" + search.toLowerCase() + "%";
    }
}