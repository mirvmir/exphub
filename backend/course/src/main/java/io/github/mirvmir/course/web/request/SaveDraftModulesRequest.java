package io.github.mirvmir.course.web.request;

import java.util.List;

public record SaveDraftModulesRequest(List<SaveDraftModuleItemRequest> modules) {
}
