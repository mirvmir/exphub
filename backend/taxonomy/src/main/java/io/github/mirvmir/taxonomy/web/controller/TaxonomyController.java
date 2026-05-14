package io.github.mirvmir.taxonomy.web.controller;

import io.github.mirvmir.taxonomy.application.service.interfaces.TaxonomyService;
import io.github.mirvmir.taxonomy.web.request.CreateTopicSuggestionRequest;
import io.github.mirvmir.taxonomy.web.response.*;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/taxonomy")
@AllArgsConstructor
public class TaxonomyController {

    private final TaxonomyService taxonomyService;

    @GetMapping("/topics/suggestions/my")
    @PreAuthorize("hasRole('USER')")
    public List<TopicSuggestionResponse> getMyTopicSuggestions() {
        return taxonomyService.getMyTopicSuggestions();
    }

    @PostMapping("/subjects/{subjectId}/sections/{sectionId}/topics/suggestions")
    @PreAuthorize("hasRole('USER')")
    public TopicSuggestionResponse suggestTopic(
            @PathVariable("subjectId")
            Long subjectId,
            @PathVariable("sectionId")
            Long sectionId,
            @Valid
            @RequestBody
            CreateTopicSuggestionRequest request
    ) {
        return taxonomyService.suggestTopic(subjectId, sectionId, request);
    }

    @GetMapping("/subjects")
    public List<SubjectResponse> getSubjects() {
        return taxonomyService.getSubjects();
    }

    @GetMapping("/subjects/{subjectId}")
    public SubjectDetailsResponse getSubject(
            @PathVariable("subjectId")
            Long subjectId
    ) {
        return taxonomyService.getSubject(subjectId);
    }

    @GetMapping("/subjects/{subjectId}/sections/{sectionId}")
    public SectionDetailsResponse getSection(
            @PathVariable("subjectId")
            Long subjectId,
            @PathVariable("sectionId")
            Long sectionId
    ) {
        return taxonomyService.getSection(subjectId, sectionId);
    }

    @GetMapping("/subjects/{subjectId}/sections/{sectionId}/topics/{topicId}")
    public TopicDetailsResponse getTopic(
            @PathVariable("subjectId")
            Long subjectId,
            @PathVariable("sectionId")
            Long sectionId,
            @PathVariable("topicId")
            Long topicId
    ) {
        return taxonomyService.getTopic(subjectId, sectionId, topicId);
    }
}