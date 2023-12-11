/*
 * MortyBot - An IRC bot built on the PircBotX framework.
 * Copyright © 2022 Brian Weiss (brian@hatemachine.net)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.hatemachine.mortybot.repositories;

import java.util.Optional;

/**
 * Defines an interface for a CRUD repository. This is similar to the interface of the same name in Spring Data Core.
 *
 * @param <T> the entity class type
 * @param <ID> the type of the entity's identifier
 */
public interface CrudRepository<T, ID> {
    /**
     * Returns the count of elements in the collection.
     *
     * @return the count of elements
     */
    long count();

    /**
     * Deletes the specified entity.
     *
     * @param entity the entity to be deleted
     */
    void delete(T entity);

    /**
     * Deletes all entities from the database.
     */
    void deleteAll();

    /**
     * Deletes all entities from the database.
     */
    void deleteAll(Iterable<? extends T> entities);

    /**
     * Deletes all entities with the given IDs.
     *
     * @param ids the IDs of the entities to delete
     */
    void deleteAllById(Iterable<? extends ID> ids);

    /**
     * Deletes an entity identified by the given ID.
     *
     * @param id The ID of the entity to delete.
     */
    void deleteById(ID id);

    /**
     * Checks if an entity with the given ID exists.
     *
     * @param id the ID of the entity to check
     * @return true if an entity with the given ID exists, false otherwise
     */
    boolean existsById(ID id);

    /**
     * Returns all entities of type T.
     *
     * @return an Iterable of entities of type T.
     */
    Iterable<T> findAll();

    /**
     * Retrieves all entities matching the provided IDs.
     *
     * @param ids the Iterable of IDs representing the entities to retrieve
     * @return an Iterable of entities matching the provided IDs
     */
    Iterable<T> findAllById(Iterable<ID> ids);

    /**
     * Finds an entity by its ID.
     *
     * @param id The ID of the entity to be found.
     * @return An Optional object containing the entity if it exists, otherwise an empty Optional object.
     */
    Optional<T> findById(ID id);

    /**
     * Saves the given entity.
     *
     * @param entity the entity to be saved
     * @param <S> the type of the entity
     * @return the saved entity
     */
    <S extends T> S save(S entity);

    /**
     * Saves all entities in the given iterable.
     *
     * @param entities the iterable containing the entities to be saved
     * @param <S>      the type of entities to be saved, extending T
     * @return an iterable containing the saved entities
     */
    <S extends T> Iterable<S> saveAll(Iterable<S> entities);
}
