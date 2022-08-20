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
import net.hatemachine.mortybot.exception.ManagedChannelException;
import net.hatemachine.mortybot.exception.ManagedChannelUserException;
import net.hatemachine.mortybot.mapper.BotUserMapper;
import net.hatemachine.mortybot.mapper.ManagedChannelMapper;
import net.hatemachine.mortybot.mapper.ManagedChannelUserMapper;
import net.hatemachine.mortybot.model.ManagedChannelUser;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.dynamic.sql.select.CountDSLCompleter;
import org.mybatis.dynamic.sql.select.SelectDSLCompleter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static net.hatemachine.mortybot.exception.ManagedChannelException.Reason.UNKNOWN_CHANNEL;
import static net.hatemachine.mortybot.mapper.ManagedChannelUserDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;

public class ManagedChannelUserDaoImpl implements ManagedChannelUserDao {

    private static final Logger log = LoggerFactory.getLogger(ManagedChannelUserDaoImpl.class);
    private static final SqlSessionFactory sqlSessionFactory = MyBatisUtil.getSqlSessionFactory();

    /**
     * Get a managed channel user by its id.
     *
     * @param id the id of the managed channel user you want to retrieve
     * @return an {@link Optional} containing a {@link ManagedChannelUser} if one exists with that id
     */
    @Override
    public synchronized Optional<ManagedChannelUser> get(final Integer id) {
        Optional<ManagedChannelUser> optionalManagedChannelUser;

        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelUserMapper mapper = session.getMapper(ManagedChannelUserMapper.class);
            optionalManagedChannelUser = mapper.selectOne(c -> c.where(managedChannelUserId, isEqualTo(id)));
        }

        return optionalManagedChannelUser;
    }

    /**
     * Add a new managed channel user.
     *
     * @param managedChannelUser the managed channel user to be added
     * @throws BotUserException if bot user does not exist
     * @throws ManagedChannelException if managed channel does not exist
     * @throws ManagedChannelUserException if the user already has an entry for this channel
     */
    @Override
    public synchronized int add(final ManagedChannelUser managedChannelUser) throws BotUserException, ManagedChannelException, ManagedChannelUserException {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelUserMapper mapper = session.getMapper(ManagedChannelUserMapper.class);

            validateBotUser(session, managedChannelUser.getBotUserId());
            validateManagedChannel(session, managedChannelUser.getManagedChannelId());

            long matchingUsers = mapper.count(c -> c.where(botUserId, isEqualTo(managedChannelUser.getBotUserId())));
            if (matchingUsers > 0) {
                throw new ManagedChannelUserException(ManagedChannelUserException.Reason.USER_EXISTS,
                        String.format("User already has an entry for this channel (managedChannelId: %d, managedChannelUserId: %d, botUserId: %d)",
                                managedChannelUser.getManagedChannelId(),
                                managedChannelUser.getManagedChannelUserId(),
                                managedChannelUser.getBotUserId()));
            }

            int rows = mapper.insert(managedChannelUser);
            session.commit();
            return rows;
        }
    }

    /**
     * Update an existing managed channel user.
     *
     * @param managedChannelUser the managed channel user to be updated
     * @throws BotUserException if bot user does not exist
     * @throws ManagedChannelException if managed channel does not exist
     * @throws ManagedChannelUserException if managed channel user does not exist
     */
    @Override
    public synchronized int update(final ManagedChannelUser managedChannelUser) throws BotUserException, ManagedChannelException, ManagedChannelUserException {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelUserMapper mapper = session.getMapper(ManagedChannelUserMapper.class);

            validateBotUser(session, managedChannelUser.getBotUserId());
            validateManagedChannel(session, managedChannelUser.getManagedChannelId());
            validateManagedChannelUser(session, managedChannelUser.getManagedChannelUserId());

            int rows = mapper.updateByPrimaryKey(managedChannelUser);
            session.commit();
            return rows;
        }
    }

    /**
     * Delete a managed channel user.
     *
     * @param managedChannelUser the managed channel user to be deleted
     * @throws ManagedChannelUserException if managed channel user does not exist
     */
    @Override
    public synchronized int delete(final ManagedChannelUser managedChannelUser) throws ManagedChannelUserException {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelUserMapper mapper = session.getMapper(ManagedChannelUserMapper.class);

            validateManagedChannelUser(session, managedChannelUser.getManagedChannelUserId());

            int rows = mapper.deleteByPrimaryKey(managedChannelUser.getManagedChannelUserId());
            session.commit();
            return rows;
        }
    }

    /**
     * Get all managed channel users.
     *
     * @return a {@link List} of {@link ManagedChannelUser} objects
     */
    @Override
    public synchronized List<ManagedChannelUser> getAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelUserMapper mapper = session.getMapper(ManagedChannelUserMapper.class);
            return mapper.select(SelectDSLCompleter.allRows());
        }
    }

    /**
     * Get a total count of all managed channel users.
     *
     * @return total number of managed channel users
     */
    public synchronized long count() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelUserMapper mapper = session.getMapper(ManagedChannelUserMapper.class);
            return mapper.count(CountDSLCompleter.allRows());
        }
    }

    /**
     * Validate that a bot user exists.
     *
     * @param session the {@link SqlSession} object
     * @param id the id of the bot user you want to check
     * @throws BotUserException if bot user does not exist
     */
    private synchronized void validateBotUser(SqlSession session, int id) throws BotUserException {
        BotUserMapper botUserMapper = session.getMapper(BotUserMapper.class);
        long matchingBotUsers = botUserMapper.count(c -> c.where(botUserId, isEqualTo(id)));

        if (matchingBotUsers == 0) {
            throw new BotUserException(BotUserException.Reason.UNKNOWN_USER, "No bot user with id " + id);
        }
    }

    /**
     * Validate that a managed channel exists.
     *
     * @param session the {@link SqlSession} object
     * @param id the id of the managed channel you want to check
     * @throws ManagedChannelException if managed channel does not exist
     */
    private synchronized void validateManagedChannel(SqlSession session, int id) throws ManagedChannelException {
        ManagedChannelMapper managedChannelMapper = session.getMapper(ManagedChannelMapper.class);
        long matchingRecords = managedChannelMapper.count(c -> c.where(managedChannelId, isEqualTo(id)));

        if (matchingRecords == 0) {
            throw new ManagedChannelException(UNKNOWN_CHANNEL, "No managed channel with id " + id);
        }
    }

    /**
     * Validate that a managed channel user exists.
     *
     * @param session the {@link SqlSession} object
     * @param id the id of the managed channel user you want to check
     * @throws ManagedChannelUserException if managed channel user does not exist
     */
    private synchronized void validateManagedChannelUser(SqlSession session, int id) throws ManagedChannelUserException {
        ManagedChannelUserMapper managedChannelUserMapper = session.getMapper(ManagedChannelUserMapper.class);
        Optional<ManagedChannelUser> optUser = managedChannelUserMapper.selectOne(c -> c.where(managedChannelUserId, isEqualTo(id)));

        if (optUser.isEmpty()) {
            throw new ManagedChannelUserException(ManagedChannelUserException.Reason.UNKNOWN_USER, "No managed channel user with id " + id);
        }
    }
}
