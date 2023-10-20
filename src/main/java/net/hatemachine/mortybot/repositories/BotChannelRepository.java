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

public class BotChannelRepository implements CrudRepository<BotChannel, Long> {

    private final SessionFactory sessionFactory;

    public BotChannelRepository() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    @Override
    public long count() {
        return sessionFactory.fromTransaction(session -> session.createNativeQuery("select count(*) from BotChannel", Long.class).uniqueResult());
    }

    @Override
    public void delete(BotChannel botChannel) {
        sessionFactory.inTransaction(session -> session.remove(botChannel));
    }

    @Override
    public void deleteAll() {
        sessionFactory.inTransaction(session -> session.createNativeQuery("delete from BotChannel", BotChannel.class));
    }

    @Override
    public void deleteAll(Iterable<? extends BotChannel> botChannels) {
        sessionFactory.inTransaction(session -> {
            for (BotChannel botChannel : botChannels) {
                session.remove(botChannel);
            }
        });
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        sessionFactory.inTransaction(session -> {
            var query = session.createNativeQuery("delete from BotChannel where id in :ids", BotChannel.class);
            query.setParameter("ids", ids);
            query.executeUpdate();
        });
    }

    @Override
    public void deleteById(Long id) {
        sessionFactory.inTransaction(session -> {
            var query = session.createNativeQuery("delete from BotChannel where id = :id", BotChannel.class);
            query.setParameter("id", id);
            query.executeUpdate();
        });
    }

    @Override
    public boolean existsById(Long id) {
        return sessionFactory.fromTransaction(session -> {
            var query = session.createNativeQuery("select 1 from BotChannel where id = :id", Integer.class);
            query.setParameter("id", id);
            return query.uniqueResult() != null;
        });
    }

    public boolean existsByName(String name) {
        return sessionFactory.fromTransaction(session -> {
            var query = session.createNativeQuery("select 1 from BotChannel where name = :name", Integer.class);
            query.setParameter("name", name);
            return query.uniqueResult() != null;
        });
    }

    @Override
    public List<BotChannel> findAll() {
        return sessionFactory.fromTransaction(session -> {
            CriteriaQuery<BotChannel> criteria = session.getCriteriaBuilder().createQuery(BotChannel.class);
            criteria.from(BotChannel.class);
            return session.createQuery(criteria).list();
        });
    }

    @Override
    public List<BotChannel> findAllById(Iterable<Long> ids) {
        return sessionFactory.fromTransaction(session -> {
            var query = session.createSelectionQuery("from BotChannel where id in :ids", BotChannel.class);
            query.setParameter("ids", ids);
            return query.getResultList();
        });
    }

    public List<BotChannel> findAllByName(Iterable<String> names) {
        return sessionFactory.fromTransaction(session -> {
            var query = session.createSelectionQuery("from BotChannel where name in :names", BotChannel.class);
            query.setParameter("names", names);
            return query.getResultList();
        });
    }

    public List<BotChannel> findAutoJoinChannels() {
        return sessionFactory.fromTransaction(session -> {
            var query = session.createSelectionQuery("from BotChannel where autoJoinFlag = :autoJoinFlag", BotChannel.class);
            query.setParameter("autoJoinFlag", true);
            return query.getResultList();
        });
    }

    @Override
    public Optional<BotChannel> findById(Long id) {
        var botChannel = sessionFactory.fromTransaction(session -> session.find(BotChannel.class, id));
        return botChannel == null ? Optional.empty() : Optional.of(botChannel);
    }

    public Optional<BotChannel> findByName(String name) {
        var botChannel = sessionFactory.fromTransaction(session -> session.bySimpleNaturalId(BotChannel.class).load(name));
        return botChannel == null ? Optional.empty() : Optional.of(botChannel);
    }

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
