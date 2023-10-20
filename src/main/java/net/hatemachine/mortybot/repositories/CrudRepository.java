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
    long count();
    void delete(T entity);
    void deleteAll();
    void deleteAll(Iterable<? extends T> entities);
    void deleteAllById(Iterable<? extends ID> ids);
    void deleteById(ID id);
    boolean existsById(ID id);
    Iterable<T> findAll();
    Iterable<T> findAllById(Iterable<ID> ids);
    Optional<T> findById(ID id);
    <S extends T> S save(S entity);
    <S extends T> Iterable<S> saveAll(Iterable<S> entities);
}
