package io.github.mirvmir.activity.application.service.port.repository;

import io.github.mirvmir.activity.domain.Activity;

import java.util.List;
import java.util.Set;

public interface ActivityRepository {
    Activity saveOrUpdate(Activity activity);
    Activity findById(Long id);
    List<Activity> findByIds(Set<Long> activityIds);
    List<Activity> findByAuthorId(Long authorId);
    Activity findActiveById(Long id);
}
