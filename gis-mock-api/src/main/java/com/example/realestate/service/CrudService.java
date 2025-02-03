package com.example.realestate.service;

import java.util.List;

/**
 * CrudService interface provides CRUD operations for entities.
 *
 * @param <T>  the entity type
 * @param <ID> the entity ID type
 */
public interface CrudService<T, ID> {

    /**
     * Saves the given entity.
     *
     * @param entity the entity to save
     * @return the saved entity
     */
    T create(T entity);

    /**
     * Finds an entity by its ID.
     *
     * @param id the ID of the entity to find
     * @return the found entity, or null if no entity found
     */
    T getById(ID id);

    /**
     * Finds all entities.
     *
     * @return a list of all entities
     */
    List<T> getAll();

    /**
     * Updates an entity with the given ID.
     *
     * @param id            the ID of the entity to update
     * @param updatedEntity the updated entity data
     * @return the updated entity
     */
    T update(ID id, T updatedEntity);

    /**
     * Deletes an entity by its ID.
     *
     * @param id the ID of the entity to delete
     */
    void delete(ID id);
}
