package com.sprint.mission.discodeit.repository.base;

import java.util.List;
import java.util.Optional;

public interface Repository<T, R> {
    T save(T entity);
    Optional<T> findById(R id);
    List<T> findAll();
    void deleteById(R id);
}
