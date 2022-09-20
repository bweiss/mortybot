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

import net.hatemachine.mortybot.custom.mapper.BotUserCustomMapper;
import net.hatemachine.mortybot.mapper.BotUserDynamicSqlSupport;
import net.hatemachine.mortybot.mapper.BotUserMapper;
import net.hatemachine.mortybot.mapper.ManagedChannelUserDynamicSqlSupport;
import net.hatemachine.mortybot.mapper.ManagedChannelUserMapper;
import net.hatemachine.mortybot.model.BotUser;
import net.hatemachine.mortybot.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mybatis.dynamic.sql.SqlBuilder.*;

/**
 * DAO class for interacting with bot users.
 */
public class BotUserDao {

    private final SqlSessionFactory sqlSessionFactory;

    public BotUserDao() {
        this.sqlSessionFactory = MyBatisUtil.getSqlSessionFactory();
    }

    /**
     * Creates a new bot user.
     *
     * @param botUser the bot user to create
     * @return the bot user after creation
     */
    public synchronized BotUser create(BotUser botUser) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BotUserMapper mapper = session.getMapper(BotUserMapper.class);
            mapper.insert(botUser);
            session.commit();
            return botUser;
        }
    }

    /**
     * Creates new bot users from a list.
     *
     * @param botUsers the list of bot users to create
     * @return the list of bot users that were created
     */
    public synchronized List<BotUser> batchCreate(List<BotUser> botUsers) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BotUserMapper mapper = session.getMapper(BotUserMapper.class);
            mapper.insertMultiple(botUsers);
            session.commit();
            return botUsers;
        }
    }

    /**
     * Creates new bot user if it does not already exist.
     *
     * @param botUser the bot user to create
     * @return true if the user was created, or false if not
     */
    public synchronized boolean createIfNotExist(BotUser botUser) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BotUserCustomMapper mapper = session.getMapper(BotUserCustomMapper.class);
            int count = mapper.ignoreInsert(botUser);
            session.commit();
            return count > 0;
        }
    }

    /**
     * Creates new bot users from a list if they do not exist already.
     *
     * @param botUsers the list of bot users to create
     * @return the total count of users that were created
     */
    public synchronized int batchCreateIfNotExist(List<BotUser> botUsers) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BotUserCustomMapper mapper = session.getMapper(BotUserCustomMapper.class);
            int count = mapper.ignoreInsertMultiple(botUsers);
            session.commit();
            return count;
        }
    }

    /**
     * Updates the details of a bot user.
     *
     * @param botUser the bot user to update
     * @return the updated bot user
     */
    public synchronized BotUser update(BotUser botUser) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BotUserMapper mapper = session.getMapper(BotUserMapper.class);
            mapper.updateByPrimaryKey(botUser);
            session.commit();
            return botUser;
        }
    }

    /**
     * Deletes a bot user as well as any managed channel user entries for that user.
     *
     * @param id the id of the bot user
     */
    public synchronized void delete(int id) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            // delete any managed channel user entries for this user
            ManagedChannelUserMapper mcuMapper = session.getMapper(ManagedChannelUserMapper.class);
            DeleteStatementProvider deleteStatement = deleteFrom(ManagedChannelUserDynamicSqlSupport.managedChannelUser)
                    .where(ManagedChannelUserDynamicSqlSupport.botUserId, isEqualTo(id))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            mcuMapper.delete(deleteStatement);

            // delete the user
            BotUserMapper mapper = session.getMapper(BotUserMapper.class);
            mapper.deleteByPrimaryKey(id);

            session.commit();
        }
    }

    /**
     * Retrieves a bot user by their ID.
     *
     * @param id the id of the bot user to retrieve
     * @return an optional containing the bot user, if they exist, or an empty optional if not.
     */
    public synchronized Optional<BotUser> get(Integer id) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BotUserMapper mapper = session.getMapper(BotUserMapper.class);
            return mapper.leftJoinSelectOne(c -> c.where(BotUserDynamicSqlSupport.id, isEqualTo(id)));
        }
    }

    /**
     * Retrieves a bot user by their name.
     *
     * @param name the name of the bot user to retrieve
     * @return an optional containing the bot user, if they exist, or an empty optional.
     */
    public synchronized Optional<BotUser> getWithName(String name) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BotUserMapper mapper = session.getMapper(BotUserMapper.class);
            return mapper.leftJoinSelectOne(c -> c.where(BotUserDynamicSqlSupport.name, isEqualTo(name)));
        }
    }

    /**
     * Retrieves all bot users.
     *
     * @return a list of bot users
     */
    public synchronized List<BotUser> getAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BotUserMapper mapper = session.getMapper(BotUserMapper.class);
            return mapper.leftJoinSelect(c -> c);
        }
    }

    /**
     * Retrieves multiple bot users from a list of IDs.
     *
     * @param idList list of bot user IDs
     * @return a list of bot users that have matching IDs
     */
    public synchronized List<BotUser> getMultipleWithIdList(List<Integer> idList) {
        if (idList == null || idList.isEmpty()) {
            return new ArrayList<>();
        }
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BotUserMapper mapper = session.getMapper(BotUserMapper.class);
            return mapper.leftJoinSelect(c -> c.where(BotUserDynamicSqlSupport.id, isIn(idList)));
        }
    }

    /**
     * Retrieves multiple bot users from a list of names.
     *
     * @param nameList list of bot user names
     * @return a list of bot users that have matching names
     */
    public synchronized List<BotUser> getMultipleWithNameList(List<String> nameList) {
        if (nameList == null || nameList.isEmpty()) {
            return new ArrayList<>();
        }
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BotUserMapper mapper = session.getMapper(BotUserMapper.class);
            return mapper.leftJoinSelect(c -> c.where(BotUserDynamicSqlSupport.name, isIn(nameList)));
        }
    }

    /**
     * Retrieves the total count of all bot users.
     *
     * @return count of all bot users
     */
    public synchronized Long count() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BotUserMapper mapper = session.getMapper(BotUserMapper.class);
            return mapper.count(c -> c);
        }
    }
}
