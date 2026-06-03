package io.github.mirvmir.catalog.application.persistence.repository;

import io.github.mirvmir.catalog.application.service.port.repository.ActivityCatalogRepository;
import io.github.mirvmir.catalog.domain.ActivityCatalog;
import io.github.mirvmir.catalog.application.service.dto.CatalogFilterDto;
import io.github.mirvmir.catalog.application.persistence.entity.ActivityCatalogEntity;
import io.github.mirvmir.catalog.application.persistence.mapper.ActivityCatalogMapper;
import io.github.mirvmir.catalog.domain.Format;
import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@AllArgsConstructor
@Repository
public class HibernateActivityCatalogRepository implements ActivityCatalogRepository {

    private final SessionFactory sessionFactory;
    private final ActivityCatalogMapper activityCatalogMapper;

    @Override
    public List<ActivityCatalog> search(CatalogFilterDto filter) {
        Session session = sessionFactory.getCurrentSession();

        StringBuilder sql = new StringBuilder("""
            select distinct c.*
            from activity_catalog c
            where 1 = 1
            """);

        Map<String, Object> params = new HashMap<>();

        if (filter != null) {
            String search = prepareSearch(filter.search());

            if (search != null) {
                sql.append("""
                    and (
                    lower(c.title) like :search
                      or lower(c.author_name) like :search
                      or lower(c.short_description) like :search
                    )
                    """);
                params.put("search", search);
            }

            if (filter.minPrice() != null) {
                sql.append("""
                        and c.price_amount >= :minPrice
                        """);
                params.put("minPrice", filter.minPrice());
            }

            if (filter.maxPrice() != null) {
                sql.append("""
                        and c.price_amount <= :maxPrice
                        """);
                params.put("maxPrice", filter.maxPrice());
            }

            if (filter.minRating() != null) {
                sql.append("""
                        and c.rating_avg >= :minRating
                        """);
                params.put("minRating", filter.minRating());
            }

            if (filter.format() != null) {
                sql.append("""
                        and c.format = :format
                        """);
                params.put("format", filter.format().name());
            }

            if (filter.topicId() != null) {
                sql.append("""
                    and exists (
                    select 1
                    from activity_catalog_topic act
                    where act.activity_catalog_id = c.id
                      and act.topic_id = :topicId
                    )
                    """);
                params.put("topicId", filter.topicId());
            }

            if (filter.sectionId() != null) {
                sql.append("""
                    and exists (
                    select 1
                    from activity_catalog_section acs
                    where acs.activity_catalog_id = c.id
                      and acs.section_id = :sectionId
                    )
                    """);
                params.put("sectionId", filter.sectionId());
            }

            if (filter.subjectId() != null) {
                sql.append("""
                    and exists (
                    select 1
                    from activity_catalog_subject acsu
                    where acsu.activity_catalog_id = c.id
                      and acsu.subject_id = :subjectId
                    )
                    """);
                params.put("subjectId", filter.subjectId());
            }
        }

        sql.append("order by c.rating_avg desc nulls last");

        NativeQuery<ActivityCatalogEntity> query =
                session.createNativeQuery(sql.toString(), ActivityCatalogEntity.class);

        params.forEach(query::setParameter);

        List<ActivityCatalogEntity> entities = query.getResultList();

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
    public void saveAll(List<ActivityCatalog> activityCatalogs) {
        if (activityCatalogs == null || activityCatalogs.isEmpty()) {
            return;
        }

        Session session = sessionFactory.getCurrentSession();

        List<Long> activityIds = activityCatalogs.stream()
                .map(ActivityCatalog::getActivityId)
                .toList();

        Set<Long> existingCourseIds = session.createQuery("""
                select c.activityId
                from ActivityCatalogEntity c
                where c.activityId in :activityIds
                """, Long.class)
                .setParameter("activityIds", activityIds)
                .getResultStream()
                .collect(java.util.stream.Collectors.toSet());

        for (ActivityCatalog activityCatalog : activityCatalogs) {
            ActivityCatalogEntity entity = activityCatalogMapper.toEntity(activityCatalog);

            if (existingCourseIds.contains(activityCatalog.getActivityId())) {
                session.merge(entity);
            } else {
                session.persist(entity);
            }
        }
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
    public List<ActivityCatalog> findByAuthorId(Long authorId) {
        Session session = sessionFactory.getCurrentSession();

        return session.createQuery("""
                select a
                from ActivityCatalogEntity a
                where a.authorId = :authorId
                """, ActivityCatalogEntity.class)
                .setParameter("authorId", authorId)
                .getResultList()
                .stream()
                .map(activityCatalogMapper::toDomain)
                .toList();
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