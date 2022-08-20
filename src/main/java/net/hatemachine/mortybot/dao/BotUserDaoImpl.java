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

import net.hatemachine.mortybot.MyBatisUtil;
import net.hatemachine.mortybot.exception.BotUserException;
import net.hatemachine.mortybot.mapper.BotUserMapper;
import net.hatemachine.mortybot.model.BotUser;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.dynamic.sql.select.CountDSLCompleter;
import org.mybatis.dynamic.sql.select.SelectDSLCompleter;

import java.util.List;
import java.util.Optional;

import static net.hatemachine.mortybot.exception.BotUserException.Reason.UNKNOWN_USER;
import static net.hatemachine.mortybot.exception.BotUserException.Reason.USER_EXISTS;
import static net.hatemachine.mortybot.mapper.BotUserDynamicSqlSupport.botUserId;
import static net.hatemachine.mortybot.mapper.BotUserDynamicSqlSupport.username;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;

public class BotUserDaoImpl implements BotUserDao {

    private static final SqlSessionFactory sqlSessionFactory = MyBatisUtil.getSqlSessionFactory();

    /**
     * Get a bot user by their id.
     *
     * @param id the id of the bot user you want to retrieve
     * @return an {@link Optional} containing a {@link BotUser} if one exists with that id
     */
    @Override
    public synchronized Optional<BotUser> get(final Integer id) {
        Optional<BotUser> optionalBotUser;

        try (SqlSession session = sqlSessionFactory.openSession()) {
            BotUserMapper mapper = session.getMapper(BotUserMapper.class);
            optionalBotUser = mapper.selectOne(c -> c.where(botUserId, isEqualTo(id)));
        }

        return optionalBotUser;
    }

    /**
     * Get a bot user by their username.
     *
     * @param uname the username of the bot user you want to retrieve
     * @return an {@link Optional} containing a {@link BotUser} if one exists with that username
     */
    @Override
    public synchronized Optional<BotUser> getByUsername(final String uname) {
        Optional<BotUser> optionalBotUser;

        try (SqlSession session = sqlSessionFactory.openSession()) {
            BotUserMapper mapper = session.getMapper(BotUserMapper.class);
            optionalBotUser = mapper.selectOne(c -> c.where(username, isEqualTo(uname)));
        }

        return optionalBotUser;
    }

    /**
     * Add a new user to the bot.
     *
     * @param botUser the bot user to be added
     * @throws BotUserException if the user already exists
     */
    @Override
    public synchronized int add(final BotUser botUser) throws BotUserException {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BotUserMapper mapper = session.getMapper(BotUserMapper.class);
            long matchingUsers = mapper.count(c -> c.where(username, isEqualTo(botUser.getUsername())));

            if (matchingUsers > 0) {
                throw new BotUserException(USER_EXISTS, botUser.toString());
            }

            int rows = mapper.insert(botUser);
            session.commit();
            return rows;
        }
    }

    /**
     * Update an existing bot user.
     *
     * @param botUser the bot user to be updated
     * @throws BotUserException if the user does not exist
     */
    @Override
    public synchronized int update(final BotUser botUser) throws BotUserException {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BotUserMapper mapper = session.getMapper(BotUserMapper.class);
            long matchingUsers = mapper.count(c -> c.where(botUserId, isEqualTo(botUser.getBotUserId())));

            if (matchingUsers == 0) {
                throw new BotUserException(UNKNOWN_USER, botUser.toString());
            }

            int rows = mapper.updateByPrimaryKey(botUser);
            session.commit();
            return rows;
        }
    }

    /**
     * Delete a bot user.
     *
     * @param botUser the bot user to be deleted
     * @throws BotUserException if the user does not exist
     */
    @Override
    public synchronized int delete(final BotUser botUser) throws BotUserException {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BotUserMapper mapper = session.getMapper(BotUserMapper.class);
            long matchingUsers = mapper.count(c -> c.where(botUserId, isEqualTo(botUser.getBotUserId())));

            if (matchingUsers == 0) {
                throw new BotUserException(UNKNOWN_USER, botUser.toString());
            }

            int rows = mapper.deleteByPrimaryKey(botUser.getBotUserId());
            session.commit();
            return rows;
        }
    }

    /**
     * Get all bot users.
     *
     * @return a list of bot users
     */
    @Override
    public synchronized List<BotUser> getAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BotUserMapper mapper = session.getMapper(BotUserMapper.class);
            return mapper.select(SelectDSLCompleter.allRows());
        }
    }

    /**
     * Get all the bot users that match a particular hostmask.
     *
     * @param hostmask the user's hostmask
     * @return list of bot users with matching hostmasks
     */
    public synchronized List<BotUser> getAll(String hostmask) {
        return getAll().stream()
                .filter(u -> u.hasMatchingHostmask(hostmask))
                .toList();
    }

    /**
     * Get a total count of all bot users.
     *
     * @return number of total bot users
     */
    public synchronized long count() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BotUserMapper mapper = session.getMapper(BotUserMapper.class);
            return mapper.count(CountDSLCompleter.allRows());
        }
    }
}
