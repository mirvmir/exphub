package io.github.mirvmir.course.web.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SaveDraftModulesRequest(
        @NotNull
        List<@Valid SaveDraftModuleItemRequest> modules
) {
}
