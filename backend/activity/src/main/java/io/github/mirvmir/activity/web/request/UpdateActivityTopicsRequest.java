package io.github.mirvmir.activity.web.request;

import java.util.Set;

public record UpdateActivityTopicsRequest(Set<Long> topicIds) {
}
