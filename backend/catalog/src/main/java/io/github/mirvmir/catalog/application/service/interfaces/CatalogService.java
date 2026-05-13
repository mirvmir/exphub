package io.github.mirvmir.catalog.application.service.interfaces;

import io.github.mirvmir.catalog.web.response.CatalogItemResponse;
import io.github.mirvmir.catalog.application.service.dto.CatalogFilterDto;

import java.util.List;

public interface CatalogService {
    List<CatalogItemResponse> getCatalog(CatalogFilterDto filter);
    void addScore(Long activityId,
                  Long courseId,
                  Double score);
}
