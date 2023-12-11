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

import net.hatemachine.mortybot.model.BotUser;
import net.hatemachine.mortybot.util.HibernateUtil;
import net.hatemachine.mortybot.util.StringUtils;
import org.hibernate.SessionFactory;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Repository for managing BotUser entities.
 * It implements the CrudRepository interface, providing basic CRUD operations for BotUser objects.
 */
public class BotUserRepository implements CrudRepository<BotUser, Long> {

    private final SessionFactory sessionFactory;

    public BotUserRepository() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    /**
     * Retrieves the total number of BotUser entities in the database.
     *
     * @return the total number of BotUser entities in the database.
     */
    @Override
    public long count() {
        return sessionFactory.fromTransaction(session -> session.createNativeQuery("select count(*) from BotUser", Long.class).uniqueResult());
    }

    /**
     * Deletes the specified BotUser from the database.
     *
     * @param botUser the BotUser to be deleted
     */
    public void delete(BotUser botUser) {
        sessionFactory.inTransaction(session -> session.remove(botUser));
    }

    /**
     * Deletes all BotUser entities from the database.
     */
    @Override
    public void deleteAll() {
        sessionFactory.inTransaction(session -> {
            session.createNativeQuery("delete from BotUser_autoOpChannels", BotUser.class).executeUpdate();
            session.createNativeQuery("delete from BotUser_hostmasks", BotUser.class).executeUpdate();
            session.createNativeQuery("delete from BotUser", BotUser.class).executeUpdate();
        });
    }

    /**
     * Deletes multiple BotUser entities from the database by their IDs.
     *
     * @param botUsers An Iterable collection of BotUser entities to be deleted.
     */
    @Override
    public void deleteAll(Iterable<? extends BotUser> botUsers) {
        sessionFactory.inTransaction(session -> {
            for (BotUser botUser : botUsers) {
                session.remove(botUser);
            }
        });
    }

    /**
     * Deletes multiple BotUser records from the database based on their IDs.
     *
     * @param ids An iterable of Long objects representing the IDs of the BotUser records to be deleted.
     * @throws org.hibernate.HibernateException If an error occurs during the database operation.
     */
    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        sessionFactory.inTransaction(session -> {
            var autoOpQuery = session.createNativeQuery("delete from BotUser_autoOpChannels where BotUser_id in :ids", BotUser.class);
            autoOpQuery.setParameter("ids", ids);
            autoOpQuery.executeUpdate();

            var hostmasksQuery = session.createNativeQuery("delete from BotUser_hostmasks where BotUser_id in :ids", BotUser.class);
            hostmasksQuery.setParameter("ids", ids);
            hostmasksQuery.executeUpdate();

            var usersQuery = session.createNativeQuery("delete from BotUser where id in :ids", BotUser.class);
            usersQuery.setParameter("ids", ids);
            usersQuery.executeUpdate();
        });
    }

    /**
     * Deletes a BotUser entity from the database based on the provided ID.
     *
     * @param id The ID of the entity to be deleted.
     */
    @Override
    public void deleteById(Long id) {
        sessionFactory.inTransaction(session -> {
            var autoOpQuery = session.createNativeQuery("delete from BotUser_autoOpChannels where BotUser_id = :id", BotUser.class);
            autoOpQuery.setParameter("id", id);
            autoOpQuery.executeUpdate();

            var hostmasksQuery = session.createNativeQuery("delete from BotUser_hostmasks where BotUser_id = :id", BotUser.class);
            hostmasksQuery.setParameter("id", id);
            hostmasksQuery.executeUpdate();

            var userQuery = session.createNativeQuery("delete from BotUser where id = :id", BotUser.class);
            userQuery.setParameter("id", id);
            userQuery.executeUpdate();
        });
    }

    /**
     * Determines whether a record with the specified ID exists in the BotUser table.
     *
     * @param id the ID of the record to check
     * @return true if a record with the specified ID exists, false otherwise
     */
    @Override
    public boolean existsById(Long id) {
        return sessionFactory.fromTransaction(session -> {
            var query = session.createNativeQuery("select 1 from BotUser where id = :id", Integer.class);
            query.setParameter("id", id);
            return query.uniqueResult() != null;
        });
    }

    /**
     * Checks if a user with the specified name exists in the database.
     *
     * @param name the name of the user to check
     * @return true if a user with the specified name exists, false otherwise
     */
    public boolean existsByName(String name) {
        return sessionFactory.fromTransaction(session -> {
            var query = session.createNativeQuery("select 1 from BotUser where name = :name", Integer.class);
            query.setParameter("name", name);
            return query.uniqueResult() != null;
        });
    }

    /**
     * Retrieves all the BotUser entities from the database.
     *
     * @return A list of BotUser entities representing all the records in the database.
     */
    @Override
    public List<BotUser> findAll() {
        return sessionFactory.fromTransaction(session -> {
            var query = session.createSelectionQuery("from BotUser", BotUser.class);
            return query.getResultList();
        });
    }

    /**
     * Retrieves a list of BotUser entities by their corresponding IDs.
     *
     * @param ids The IDs of the BotUser entities to be retrieved.
     * @return A list of BotUser entities matching the given IDs.
     */
    @Override
    public List<BotUser> findAllById(Iterable<Long> ids) {
        return sessionFactory.fromTransaction(session -> {
            var query = session.createSelectionQuery("from BotUser where id in :ids", BotUser.class);
            query.setParameter("ids", ids);
            return query.getResultList();
        });
    }

    /**
     * Find all BotUser objects based on the provided names.
     *
     * @param names A collection of names to search for.
     * @return A list of BotUser objects matching the provided names.
     */
    public List<BotUser> findAllByName(Iterable<String> names) {
        return sessionFactory.fromTransaction(session -> {
            var query = session.createSelectionQuery("from BotUser where name in :names", BotUser.class);
            query.setParameter("names", names);
            return query.getResultList();
        });
    }

    /**
     * Finds a BotUser by hostmask.
     *
     * @param userHostmask the user hostmask to search for
     * @return an Optional containing the BotUser if found, or an empty Optional if not found
     */
    public Optional<BotUser> findByHostmask(String userHostmask) {
        return sessionFactory.fromTransaction(session -> {
            Optional<BotUser> botUser = Optional.empty();

            var query = session.createNativeQuery("select * from BotUser_hostmasks", Object.class);
            var results = query.getResultList();

            if (!results.isEmpty()) {
                for (Object result : results) {
                    Object[] row = (Object[]) result;
                    int id = (int) row[0];
                    String hostmask = (String) row[1];

                    var pattern = Pattern.compile(StringUtils.wildcardToRegex(hostmask.toLowerCase()));
                    var matcher = pattern.matcher(userHostmask.toLowerCase());

                    if (matcher.matches()) {
                        botUser = Optional.of(session.find(BotUser.class, id));
                        break; // we only care about the first match
                    }
                }
            }

            return botUser;
        });
    }

    /**
     * Finds a BotUser by its ID.
     *
     * @param id the ID of the BotUser to find
     * @return an Optional containing the BotUser if found, or an empty Optional if not found
     */
    @Override
    public Optional<BotUser> findById(Long id) {
        var botUser = sessionFactory.fromTransaction(session -> session.find(BotUser.class, id));
        return botUser == null ? Optional.empty() : Optional.of(botUser);
    }

    /**
     * Finds a BotUser by their name.
     *
     * @param name the name of the BotUser to find
     * @return an Optional containing the BotUser if found, or an empty Optional if not found
     */
    public Optional<BotUser> findByName(String name) {
        var botUser = sessionFactory.fromTransaction(session -> session.bySimpleNaturalId(BotUser.class).load(name));
        return botUser == null ? Optional.empty() : Optional.of(botUser);
    }

    /**
     * Saves a BotUser entity to the database.
     *
     * @param botUser The BotUser object to be saved. Cannot be null.
     * @return The saved BotUser object.
     * @throws NullPointerException if botUser is null.
     */
    @Override
    public <S extends BotUser> S save(S botUser) {
        Objects.requireNonNull(botUser, "botUser cannot be null");

        return sessionFactory.fromTransaction(session -> {
            if (botUser.getId() == null) {
                session.persist(botUser);
            } else {
                session.merge(botUser);
            }
            return botUser;
        });
    }

    /**
     * Saves all the given bot users to the database.
     *
     * @param botUsers the iterable of bot users to save
     * @param <S> the type of bot user
     * @return the iterable of saved bot users
     */
    @Override
    public <S extends BotUser> Iterable<S> saveAll(Iterable<S> botUsers) {
        return sessionFactory.fromTransaction(session -> {
            for (BotUser botUser : botUsers) {
                session.persist(botUser);
            }
            return botUsers;
        });
    }
}
