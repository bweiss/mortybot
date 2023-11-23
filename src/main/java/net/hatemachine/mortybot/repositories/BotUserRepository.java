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

public class BotUserRepository implements CrudRepository<BotUser, Long> {

    private final SessionFactory sessionFactory;

    public BotUserRepository() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    @Override
    public long count() {
        return sessionFactory.fromTransaction(session -> session.createNativeQuery("select count(*) from BotUser", Long.class).uniqueResult());
    }

    public void delete(BotUser botUser) {
        sessionFactory.inTransaction(session -> session.remove(botUser));
    }

    @Override
    public void deleteAll() {
        sessionFactory.inTransaction(session -> {
            session.createNativeQuery("delete from BotUser_autoOpChannels", BotUser.class).executeUpdate();
            session.createNativeQuery("delete from BotUser_hostmasks", BotUser.class).executeUpdate();
            session.createNativeQuery("delete from BotUser", BotUser.class).executeUpdate();
        });
    }

    @Override
    public void deleteAll(Iterable<? extends BotUser> botUsers) {
        sessionFactory.inTransaction(session -> {
            for (BotUser botUser : botUsers) {
                session.remove(botUser);
            }
        });
    }

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

    @Override
    public boolean existsById(Long id) {
        return sessionFactory.fromTransaction(session -> {
            var query = session.createNativeQuery("select 1 from BotUser where id = :id", Integer.class);
            query.setParameter("id", id);
            return query.uniqueResult() != null;
        });
    }

    public boolean existsByName(String name) {
        return sessionFactory.fromTransaction(session -> {
            var query = session.createNativeQuery("select 1 from BotUser where name = :name", Integer.class);
            query.setParameter("name", name);
            return query.uniqueResult() != null;
        });
    }

    @Override
    public List<BotUser> findAll() {
        return sessionFactory.fromTransaction(session -> {
            var query = session.createSelectionQuery("from BotUser", BotUser.class);
            return query.getResultList();
        });
    }

    @Override
    public List<BotUser> findAllById(Iterable<Long> ids) {
        return sessionFactory.fromTransaction(session -> {
            var query = session.createSelectionQuery("from BotUser where id in :ids", BotUser.class);
            query.setParameter("ids", ids);
            return query.getResultList();
        });
    }

    public List<BotUser> findAllByName(Iterable<String> names) {
        return sessionFactory.fromTransaction(session -> {
            var query = session.createSelectionQuery("from BotUser where name in :names", BotUser.class);
            query.setParameter("names", names);
            return query.getResultList();
        });
    }

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

    @Override
    public Optional<BotUser> findById(Long id) {
        var botUser = sessionFactory.fromTransaction(session -> session.find(BotUser.class, id));
        return botUser == null ? Optional.empty() : Optional.of(botUser);
    }

    public Optional<BotUser> findByName(String name) {
        var botUser = sessionFactory.fromTransaction(session -> session.bySimpleNaturalId(BotUser.class).load(name));
        return botUser == null ? Optional.empty() : Optional.of(botUser);
    }

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
