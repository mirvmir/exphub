package io.github.mirvmir.activity.application.service.interfaces;

import io.github.mirvmir.activity.web.request.RejectActivityRequest;

public interface ModerationActivityService {
    void approve(Long id);
    void reject(Long id, RejectActivityRequest request);
    void block(Long id);
}
