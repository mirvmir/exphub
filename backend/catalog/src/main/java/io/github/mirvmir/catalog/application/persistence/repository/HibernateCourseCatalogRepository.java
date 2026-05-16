package io.github.mirvmir.catalog.application.persistence.repository;

import io.github.mirvmir.catalog.application.persistence.entity.ActivityCatalogEntity;
import io.github.mirvmir.catalog.application.service.port.repository.CourseCatalogRepository;
import io.github.mirvmir.catalog.application.service.dto.CatalogFilterDto;
import io.github.mirvmir.catalog.domain.ActivityCatalog;
import io.github.mirvmir.catalog.domain.CourseCatalog;
import io.github.mirvmir.catalog.application.persistence.entity.CourseCatalogEntity;
import io.github.mirvmir.catalog.application.persistence.mapper.CourseCatalogMapper;
import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
@Repository
public class HibernateCourseCatalogRepository implements CourseCatalogRepository {

    private final SessionFactory sessionFactory;
    private final CourseCatalogMapper courseCatalogMapper;

    @Override
    public List<CourseCatalog> search(CatalogFilterDto filter) {
        Session session = sessionFactory.getCurrentSession();

        String sql = """
                select distinct c.*
                from course_catalog c
                where (:search is null
                       or lower(c.title) like :search
                       or lower(c.author_name) like :search
                       or lower(c.short_description) like :search)
                  and (:minPrice is null or c.price_amount >= :minPrice)
                  and (:maxPrice is null or c.price_amount <= :maxPrice)
                  and (:minRating is null or c.rating_avg >= :minRating)
                  and (:topicId is null or exists (
                       select 1
                       from course_catalog_topic ct
                       where ct.course_catalog_id = c.id
                         and ct.topic_id = :topicId
                  ))
                  and (:sectionId is null or exists (
                       select 1
                       from course_catalog_section cs
                       where cs.course_catalog_id = c.id
                         and cs.section_id = :sectionId
                ))
                  and (:subjectId is null or exists (
                       select 1
                       from course_catalog_subject csu
                       where csu.course_catalog_id = c.id
                         and csu.subject_id = :subjectId
                ))
                order by c.rating_avg desc nulls last
                """;

        List<CourseCatalogEntity> entities = session
                .createNativeQuery(sql, CourseCatalogEntity.class)
                .setParameter("search", prepareSearch(filter.search()))
                .setParameter("minPrice", filter.minPrice())
                .setParameter("maxPrice", filter.maxPrice())
                .setParameter("minRating", filter.minRating())
                .setParameter("topicId", filter.topicId())
                .setParameter("sectionId", filter.sectionId())
                .setParameter("subjectId", filter.subjectId())
                .getResultList();

        return entities.stream()
                .map(courseCatalogMapper::toDomain)
                .toList();
    }

    @Override
    public void saveOrUpdate(CourseCatalog courseCatalog) {
        Session session = sessionFactory.getCurrentSession();

        CourseCatalogEntity existingEntity = session.createQuery("""
                 select c
                 from CourseCatalogEntity c
                 where c.courseId = :courseId
                 """, CourseCatalogEntity.class)
                .setParameter("courseId", courseCatalog.getCourseId())
                .setMaxResults(1)
                .uniqueResult();

        CourseCatalogEntity entity =
                courseCatalogMapper.toEntity(courseCatalog);

        if (existingEntity == null) {
            session.persist(entity);
            return;
        }

        session.merge(entity);
    }

    @Override
    public void saveAll(List<CourseCatalog> courseCatalogs) {
        if (courseCatalogs == null || courseCatalogs.isEmpty()) {
            return;
        }

        Session session = sessionFactory.getCurrentSession();

        List<Long> courseIds = courseCatalogs.stream()
                .map(CourseCatalog::getCourseId)
                .toList();

        Set<Long> existingCourseIds = session.createQuery("""
                select c.courseId
                from CourseCatalogEntity c
                where c.courseId in :courseIds
                """, Long.class)
                .setParameter("courseIds", courseIds)
                .getResultStream()
                .collect(java.util.stream.Collectors.toSet());

        for (CourseCatalog courseCatalog : courseCatalogs) {
            CourseCatalogEntity entity = courseCatalogMapper.toEntity(courseCatalog);

            if (existingCourseIds.contains(courseCatalog.getCourseId())) {
                session.merge(entity);
            } else {
                session.persist(entity);
            }
        }
    }

    @Override
    public void deleteByCourseId(Long courseId) {
        Session session = sessionFactory.getCurrentSession();

        session.createMutationQuery("""
                delete from CourseCatalogEntity a
                where a.courseId = :courseId
                """)
                .setParameter("courseId", courseId)
                .executeUpdate();
    }

    @Override
    public CourseCatalog findByCourseId(Long courseId) {
        Session session = sessionFactory.getCurrentSession();

        CourseCatalogEntity entity = session.createQuery("""
                select c
                from CourseCatalogEntity c
                where c.courseId = :courseId
                """, CourseCatalogEntity.class)
                .setParameter("courseId", courseId)
                .setMaxResults(1)
                .uniqueResult();

        return courseCatalogMapper.toDomain(entity);
    }

    @Override
    public List<CourseCatalog> findByAuthorId(Long authorId) {
        Session session = sessionFactory.getCurrentSession();

        return session.createQuery("""
                select c
                from CourseCatalogEntity c
                where c.authorId = :authorId
                """, CourseCatalogEntity.class)
                .setParameter("authorId", authorId)
                .getResultList()
                .stream()
                .map(courseCatalogMapper::toDomain)
                .toList();
    }

    @Override
    public void updateTopicIds(Long courseId, Set<Long> topicIds) {
        Session session = sessionFactory.getCurrentSession();

        session.createMutationQuery("""
                update CourseCatalogEntity c
                set c.topicIds = :topicIds
                where c.courseId = :courseId
                """)
                .setParameter("courseId", courseId)
                .setParameter("topicIds", topicIds)
                .executeUpdate();
    }

    @Override
    public void updateTaxonomyIds(Long courseId,
                                  Set<Long> topicIds,
                                  Set<Long> sectionIds,
                                  Set<Long> subjectIds) {
        Session session = sessionFactory.getCurrentSession();

        CourseCatalogEntity entity = session.createQuery("""
                select c
                from CourseCatalogEntity c
                where c.courseId = :courseId
                """, CourseCatalogEntity.class)
                .setParameter("courseId", courseId)
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