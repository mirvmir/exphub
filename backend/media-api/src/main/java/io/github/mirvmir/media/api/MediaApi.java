package io.github.mirvmir.media.api;

import java.util.Set;

public interface MediaApi {
    boolean existsFileById(Long id);
    boolean existsFileByIds(Set<Long> ids);
    boolean existsVideoByIds(Set<Long> ids);
}
