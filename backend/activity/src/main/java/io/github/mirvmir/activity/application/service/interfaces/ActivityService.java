package io.github.mirvmir.activity.application.service.interfaces;

import io.github.mirvmir.activity.web.response.ActivityDescriptionResponse;

public interface ActivityService {
    ActivityDescriptionResponse getActivity(Long id);
}
