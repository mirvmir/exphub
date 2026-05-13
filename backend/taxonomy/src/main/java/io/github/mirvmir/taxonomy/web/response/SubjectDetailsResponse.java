package io.github.mirvmir.taxonomy.web.response;

import java.util.List;

public record SubjectDetailsResponse(
        Long id,
        String name,
        List<SectionShortResponse> sections
) {
}