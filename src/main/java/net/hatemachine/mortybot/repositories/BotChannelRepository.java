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

import jakarta.persistence.criteria.CriteriaQuery;
import net.hatemachine.mortybot.model.BotChannel;
import net.hatemachine.mortybot.util.HibernateUtil;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Repository for managing BotChannel entities.
 * It implements the CrudRepository interface, providing basic CRUD operations for BotChannel objects.
 */
public class BotChannelRepository implements CrudRepository<BotChannel, Long> {

    private final SessionFactory sessionFactory;

    public BotChannelRepository() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    /**
     * Retrieves the total number of records in the BotChannel table.
     *
     * @return the total number of records
     */
    @Override
    public long count() {
        return sessionFactory.fromTransaction(session -> session.createNativeQuery("select count(*) from BotChannel", Long.class).uniqueResult());
    }

    /**
     * Deletes a BotChannel entity from the database.
     *
     * @param botChannel the BotChannel object to be deleted
     */
    @Override
    public void delete(BotChannel botChannel) {
        sessionFactory.inTransaction(session -> session.remove(botChannel));
    }

    /**
     * Deletes all BotChannel entities from the database.
     */
    @Override
    public void deleteAll() {
        sessionFactory.inTransaction(session -> session.createNativeQuery("delete from BotChannel", BotChannel.class));
    }

    /**
     * Deletes all bot channels from the database.
     *
     * @param botChannels an iterable collection of bot channels to be deleted
     */
    @Override
    public void deleteAll(Iterable<? extends BotChannel> botChannels) {
        sessionFactory.inTransaction(session -> {
            for (BotChannel botChannel : botChannels) {
                session.remove(botChannel);
            }
        });
    }

    /**
     * Deletes multiple BotChannel entities from the database by their IDs.
     *
     * @param ids An {@link Iterable} of {@link Long} IDs representing the BotChannels to be deleted.
     * @throws org.hibernate.HibernateException If an error occurs during the database operation.
     */
    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        sessionFactory.inTransaction(session -> {
            var query = session.createNativeQuery("delete from BotChannel where id in :ids", BotChannel.class);
            query.setParameter("ids", ids);
            query.executeUpdate();
        });
    }

    /**
     * Deletes a BotChannel entity from the database by its id.
     *
     * @param id The id of the BotChannel entity to be deleted.
     * @throws org.hibernate.HibernateException If an error occurs during the database operation.
     */
    @Override
    public void deleteById(Long id) {
        sessionFactory.inTransaction(session -> {
            var query = session.createNativeQuery("delete from BotChannel where id = :id", BotChannel.class);
            query.setParameter("id", id);
            query.executeUpdate();
        });
    }

    /**
     * Checks if a BotChannel with the given id exists in the database.
     *
     * @param id the id of the BotChannel to check
     * @return true if a BotChannel with the given id exists, false otherwise
     */
    @Override
    public boolean existsById(Long id) {
        return sessionFactory.fromTransaction(session -> {
            var query = session.createNativeQuery("select 1 from BotChannel where id = :id", Integer.class);
            query.setParameter("id", id);
            return query.uniqueResult() != null;
        });
    }

    /**
     * Checks if a BotChannel exists by name.
     *
     * @param name the name of the BotChannel
     * @return true if a BotChannel with the given name exists, false otherwise
     */
    public boolean existsByName(String name) {
        return sessionFactory.fromTransaction(session -> {
            var query = session.createNativeQuery("select 1 from BotChannel where name = :name", Integer.class);
            query.setParameter("name", name);
            return query.uniqueResult() != null;
        });
    }

    /**
     * Retrieves all the BotChannel entities from the database.
     *
     * @return A list containing all the BotChannels.
     */
    @Override
    public List<BotChannel> findAll() {
        return sessionFactory.fromTransaction(session -> {
            CriteriaQuery<BotChannel> criteria = session.getCriteriaBuilder().createQuery(BotChannel.class);
            criteria.from(BotChannel.class);
            return session.createQuery(criteria).list();
        });
    }

    /**
     * Retrieves a list of BotChannels that match the given IDs.
     *
     * @param ids An iterable collection of Long values representing the IDs of the BotChannels to find.
     * @return A list of BotChannels that match the given IDs.
     */
    @Override
    public List<BotChannel> findAllById(Iterable<Long> ids) {
        return sessionFactory.fromTransaction(session -> {
            var query = session.createSelectionQuery("from BotChannel where id in :ids", BotChannel.class);
            query.setParameter("ids", ids);
            return query.getResultList();
        });
    }

    /**
     * Retrieves a list of BotChannels based on the given names.
     *
     * @param names an iterable collection of the names to search for
     * @return a list of BotChannels that match the given names
     */
    public List<BotChannel> findAllByName(Iterable<String> names) {
        return sessionFactory.fromTransaction(session -> {
            var query = session.createSelectionQuery("from BotChannel where name in :names", BotChannel.class);
            query.setParameter("names", names);
            return query.getResultList();
        });
    }

    /**
     * Finds all the BotChannel entities that have the auto-join flag set.
     *
     * @return a list of BotChannels that have the autoJoinFlag set to true.
     */
    public List<BotChannel> findAutoJoinChannels() {
        return sessionFactory.fromTransaction(session -> {
            var query = session.createSelectionQuery("from BotChannel where autoJoinFlag = :autoJoinFlag", BotChannel.class);
            query.setParameter("autoJoinFlag", true);
            return query.getResultList();
        });
    }

    /**
     * Retrieves a BotChannel by its ID.
     *
     * @param id the ID of the BotChannel to retrieve
     * @return an Optional containing the BotChannel if found, otherwise an empty Optional
     */
    @Override
    public Optional<BotChannel> findById(Long id) {
        var botChannel = sessionFactory.fromTransaction(session -> session.find(BotChannel.class, id));
        return botChannel == null ? Optional.empty() : Optional.of(botChannel);
    }

    /**
     * Finds a BotChannel by its name.
     *
     * @param name the name of the BotChannel to find
     * @return an Optional object containing the found BotChannel if it exists, otherwise returns an empty Optional
     */
    public Optional<BotChannel> findByName(String name) {
        var botChannel = sessionFactory.fromTransaction(session -> session.bySimpleNaturalId(BotChannel.class).load(name));
        return botChannel == null ? Optional.empty() : Optional.of(botChannel);
    }

    /**
     * Saves a BotChannel entity to the database.
     *
     * @param botChannel the BotChannel object to save (not null)
     * @param <S>        the type of the BotChannel object
     * @return the saved BotChannel object
     * @throws NullPointerException if botChannel is null
     */
    @Override
    public <S extends BotChannel> S save(S botChannel) {
        Objects.requireNonNull(botChannel, "botChannel cannot be null");

        return sessionFactory.fromTransaction(session -> {
            if (botChannel.getId() == null) {
                session.persist(botChannel);
            } else {
                session.merge(botChannel);
            }
            return botChannel;
        });
    }

    /**
     * Saves all the given BotChannel entities to the database.
     *
     * @param botChannels the bot channels to save
     * @param <S>         the type of bot channel
     * @return an iterable of the saved bot channels
     */
    @Override
    public <S extends BotChannel> Iterable<S> saveAll(Iterable<S> botChannels) {
        return sessionFactory.fromTransaction(session -> {
            for (BotChannel botChannel : botChannels) {
                session.persist(botChannel);
            }
            return botChannels;
        });
    }
}
