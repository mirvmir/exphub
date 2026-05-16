package io.github.mirvmir.taxonomy.web.controller;

import io.github.mirvmir.taxonomy.application.service.interfaces.TaxonomyModerationService;
import io.github.mirvmir.taxonomy.web.request.*;
import io.github.mirvmir.taxonomy.web.response.SectionDetailsResponse;
import io.github.mirvmir.taxonomy.web.response.SubjectResponse;
import io.github.mirvmir.taxonomy.web.response.TopicDetailsResponse;
import io.github.mirvmir.taxonomy.web.response.TopicSuggestionResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/moderation/taxonomy")
@AllArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class ModerationTaxonomyController {

    private final TaxonomyModerationService taxonomyModerationService;

    @GetMapping("/topics/suggestions")
    public List<TopicSuggestionResponse> getTopicSuggestions() {
        return taxonomyModerationService.getPendingSuggestions();
    }

    @PostMapping("/topics/suggestions/{suggestionId}/approve")
    public TopicDetailsResponse approveTopicSuggestion(
            @PathVariable("suggestionId")
            Long suggestionId
    ) {
        return taxonomyModerationService.approveSuggestion(suggestionId);
    }

    @PostMapping("/topics/suggestions/{suggestionId}/merge")
    public TopicSuggestionResponse mergeTopicSuggestion(
            @PathVariable("suggestionId")
            Long suggestionId,
            @Valid
            @RequestBody
            MergeTopicSuggestionRequest request
    ) {
        return taxonomyModerationService.mergeSuggestion(suggestionId, request);
    }

    @PostMapping("/topics/suggestions/{suggestionId}/reject")
    public TopicSuggestionResponse rejectTopicSuggestion(
            @PathVariable("suggestionId")
            Long suggestionId,
            @Valid
            @RequestBody
            RejectTopicSuggestionRequest request
    ) {
        return taxonomyModerationService.rejectSuggestion(suggestionId, request);
    }

    @PostMapping("/subjects")
    public SubjectResponse createSubject(
            @Valid
            @RequestBody
            CreateSubjectRequest request
    ) {
        return taxonomyModerationService.createSubject(request);
    }

    @PostMapping("/subjects/{subjectId}/sections")
    public SectionDetailsResponse createSection(
            @PathVariable("subjectId")
            Long subjectId,
            @Valid
            @RequestBody
            CreateSectionRequest request
    ) {
        return taxonomyModerationService.createSection(subjectId, request);
    }

    @PostMapping("/subjects/{subjectId}/sections/{sectionId}/topics")
    public TopicDetailsResponse createTopic(
            @PathVariable("subjectId")
            Long subjectId,
            @PathVariable("sectionId")
            Long sectionId,
            @Valid
            @RequestBody
            CreateTopicRequest request
    ) {
        return taxonomyModerationService.createTopic(subjectId, sectionId, request);
    }
}