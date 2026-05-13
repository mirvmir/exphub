package io.github.mirvmir.activity.application.service.port.repository;

import io.github.mirvmir.activity.domain.Activity;

public interface ActivityRepository {
    Activity saveOrUpdate(Activity activity);
    Activity findById(Long id);
}
