package io.github.mirvmir.catalog.web.controller;

import io.github.mirvmir.catalog.web.response.CatalogItemResponse;
import io.github.mirvmir.catalog.application.service.interfaces.CatalogService;
import io.github.mirvmir.catalog.application.service.dto.CatalogFilterDto;
import io.github.mirvmir.catalog.web.request.GetCatalogRequest;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/catalog")
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping
    public List<CatalogItemResponse> getCatalog(
            @ModelAttribute
            GetCatalogRequest request
    ) {
        CatalogFilterDto filter = new CatalogFilterDto(
                request.search(),
                request.topicId(),
                request.sectionId(),
                request.subjectId(),
                request.minPrice(),
                request.maxPrice(),
                request.minRating(),
                request.format(),
                request.type()
        );

        return catalogService.getCatalog(filter);
    }
}
