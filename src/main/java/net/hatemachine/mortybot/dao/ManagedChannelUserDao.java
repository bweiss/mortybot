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
package net.hatemachine.mortybot.dao;

import net.hatemachine.mortybot.custom.mapper.ManagedChannelUserCustomMapper;
import net.hatemachine.mortybot.mapper.ManagedChannelUserDynamicSqlSupport;
import net.hatemachine.mortybot.mapper.ManagedChannelUserMapper;
import net.hatemachine.mortybot.model.ManagedChannelUser;
import net.hatemachine.mortybot.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.List;
import java.util.Optional;

import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;

/**
 * DAO class for interacting with managed channel user entries. A "managed channel user" entry represents a bot user's
 * channel-specific flags for a managed channel.
 */
public class ManagedChannelUserDao {

    private final SqlSessionFactory sqlSessionFactory;

    public ManagedChannelUserDao() {
        this.sqlSessionFactory = MyBatisUtil.getSqlSessionFactory();
    }

    /**
     * Creates a new managed channel user entry.
     *
     * @param managedChannelUser the managed channel user to be created
     * @return the managed channel user that was created
     */
    public synchronized ManagedChannelUser create(ManagedChannelUser managedChannelUser) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelUserMapper mapper = session.getMapper(ManagedChannelUserMapper.class);
            mapper.insert(managedChannelUser);
            session.commit();
            return managedChannelUser;
        }
    }

    /**
     * Creates multiple managed cahnnel user entries from a list.
     *
     * @param managedChannelUsers list of managed channel users
     * @return the list of managed channel users that were created
     */
    public synchronized List<ManagedChannelUser> batchCreate(List<ManagedChannelUser> managedChannelUsers) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelUserMapper mapper = session.getMapper(ManagedChannelUserMapper.class);
            mapper.insertMultiple(managedChannelUsers);
            session.commit();
            return managedChannelUsers;
        }
    }

    /**
     * Creates a managed channel user entry if it does not yet exist.
     *
     * @param managedChannelUser the managed channel user to create
     * @return true if the entry was created, false if not
     */
    public synchronized boolean createIfNotExist(ManagedChannelUser managedChannelUser) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelUserCustomMapper mapper = session.getMapper(ManagedChannelUserCustomMapper.class);
            int count = mapper.ignoreInsert(managedChannelUser);
            session.commit();
            return count > 0;
        }
    }

    /**
     * Creates multiple managed channel user entries from a list, if they do not yet exist.
     *
     * @param managedChannelUsers the list of managed channel users to create
     * @return the total count of entries created
     */
    public synchronized int batchCreateIfNotExist(List<ManagedChannelUser> managedChannelUsers) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelUserCustomMapper mapper = session.getMapper(ManagedChannelUserCustomMapper.class);
            int count = mapper.ignoreInsertMultiple(managedChannelUsers);
            session.commit();
            return count;
        }
    }

    /**
     * Updates a managed channel user entry.
     *
     * @param managedChannelUser the managed channel user to update
     * @return the managed channel user that was updated
     */
    public synchronized ManagedChannelUser update(ManagedChannelUser managedChannelUser) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelUserMapper mapper = session.getMapper(ManagedChannelUserMapper.class);
            mapper.updateByPrimaryKey(managedChannelUser);
            session.commit();
            return managedChannelUser;
        }
    }

    /**
     * Deletes a managed channel user entry by ID.
     *
     * @param id the id of the managed channel user to delete
     */
    public synchronized void delete(int id) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelUserMapper mapper = session.getMapper(ManagedChannelUserMapper.class);
            mapper.deleteByPrimaryKey(id);
            session.commit();
        }
    }

    /**
     * Retrieves a managed channel user entry by ID.
     *
     * @param id the id of the managed channel user to retrieve
     * @return an optional containing the managed channel user, if one exists, or an empty optional if not
     */
    public synchronized Optional<ManagedChannelUser> get(Integer id) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelUserMapper mapper = session.getMapper(ManagedChannelUserMapper.class);
            return mapper.leftJoinSelectOne(c -> c.where(ManagedChannelUserDynamicSqlSupport.id, isEqualTo(id)));
        }
    }

    /**
     * Retrieves a managed channel user entry by a combination of managed channel ID and bot user ID.
     *
     * @param managedChannelId the id of the managed channel
     * @param botUserId the id of the bot user
     * @return an optional containing the managed channel user, if one exists, or an empty optional if not
     */
    public synchronized Optional<ManagedChannelUser> getWithManagedChannelIdAndBotUserId(Integer managedChannelId, Integer botUserId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelUserMapper mapper = session.getMapper(ManagedChannelUserMapper.class);
            return mapper.leftJoinSelectOne(c -> c.where(ManagedChannelUserDynamicSqlSupport.managedChannelId, isEqualTo(managedChannelId))
                    .and(ManagedChannelUserDynamicSqlSupport.botUserId, isEqualTo(botUserId)));
        }
    }

    /**
     * Retrieves all managed channel user entries.
     *
     * @return a list of managed channel users
     */
    public synchronized List<ManagedChannelUser> getAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelUserMapper mapper = session.getMapper(ManagedChannelUserMapper.class);
            return mapper.leftJoinSelect(c -> c);
        }
    }

    /**
     * Retrieves multiple managed channel user entries by bot user ID.
     *
     * @param botUserId the bot user id to retrieve entries for
     * @return a list of managed channel user entries, presumably one per channel for this user
     */
    public synchronized List<ManagedChannelUser> getMultipleWithBotUserId(Integer botUserId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelUserMapper mapper = session.getMapper(ManagedChannelUserMapper.class);
            return mapper.leftJoinSelect(c -> c.where(ManagedChannelUserDynamicSqlSupport.botUserId, isEqualTo(botUserId)));
        }
    }

    /**
     * Retrieves multiple managed channel user entries by managed channel ID.
     *
     * @param managedChannelId the managed channel id to retrieve entries for
     * @return a list of managed channel user entries, presumably one per user for this channel
     */
    public synchronized List<ManagedChannelUser> getMultipleWithManagedChannelId(Integer managedChannelId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelUserMapper mapper = session.getMapper(ManagedChannelUserMapper.class);
            return mapper.leftJoinSelect(c -> c.where(ManagedChannelUserDynamicSqlSupport.managedChannelId, isEqualTo(managedChannelId)));
        }
    }
}
