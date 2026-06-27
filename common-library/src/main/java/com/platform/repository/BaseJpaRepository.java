package com.platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

/**
 * Common repository contract used by all repositories in the project.
 * Marked with @NoRepositoryBean so Spring Data does not create a bean for this interface itself.
 */
@NoRepositoryBean
public interface BaseJpaRepository<T, ID> extends JpaRepository<T, ID> {

    /**
     * Alias for save(), provided so callers can express intent to update.
     */
    default T update(T entity) {
        return save(entity);
    }

    /**
     * Save or update alias.
     */
    default T saveOrUpdate(T entity) {
        return save(entity);
    }

    /**
     * Convenience to return Optional from findById (delegates to JpaRepository).
     */
    default Optional<T> findOptionalById(ID id) {
        return findById(id);
    }

    /**
     * Convenience to throw an exception when entity is not present.
     */
    default T findByIdOrThrow(ID id) {
        return findById(id).orElseThrow(() -> new IllegalArgumentException("Entity not found: " + id));
    }
}
