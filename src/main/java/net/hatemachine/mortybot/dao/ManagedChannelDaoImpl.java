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
import net.hatemachine.mortybot.exception.ManagedChannelException;
import net.hatemachine.mortybot.mapper.ManagedChannelMapper;
import net.hatemachine.mortybot.model.ManagedChannel;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.dynamic.sql.select.CountDSLCompleter;
import org.mybatis.dynamic.sql.select.SelectDSLCompleter;

import java.util.List;
import java.util.Optional;

import static net.hatemachine.mortybot.exception.ManagedChannelException.Reason.*;
import static net.hatemachine.mortybot.mapper.ManagedChannelDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;

public class ManagedChannelDaoImpl implements ManagedChannelDao {

    private static final SqlSessionFactory sqlSessionFactory = MyBatisUtil.getSqlSessionFactory();

    /**
     * Get a managed channel by its id.
     *
     * @param id the id of the managed channel you want to retrieve
     * @return an {@link Optional} containing a {@link ManagedChannel} if one exists with that id
     */
    @Override
    public synchronized Optional<ManagedChannel> get(final Integer id) {
        Optional<ManagedChannel> optionalManagedChannel;

        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelMapper mapper = session.getMapper(ManagedChannelMapper.class);
            optionalManagedChannel = mapper.selectOne(c -> c.where(managedChannelId, isEqualTo(id)));
        }

        return optionalManagedChannel;
    }

    /**
     * Get a managed channel by its name.
     *
     * @param channelName the name of the managed channel you want to retrieve
     * @return an {@link Optional} containing a {@link ManagedChannel} if one exists with that name
     */
    @Override
    public synchronized Optional<ManagedChannel> getByName(final String channelName) {
        Optional<ManagedChannel> optionalManagedChannel;

        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelMapper mapper = session.getMapper(ManagedChannelMapper.class);
            optionalManagedChannel = mapper.selectOne(c -> c.where(name, isEqualTo(channelName)));
        }

        return optionalManagedChannel;
    }

    /**
     * Add a new managed channel.
     *
     * @param managedChannel the managed channel to be added
     * @throws ManagedChannelException if managed channel already exists
     */
    @Override
    public synchronized int add(final ManagedChannel managedChannel) throws ManagedChannelException {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelMapper mapper = session.getMapper(ManagedChannelMapper.class);
            long matchingChannels = mapper.count(c -> c.where(name, isEqualTo(managedChannel.getName())));

            if (matchingChannels > 0) {
                throw new ManagedChannelException(CHANNEL_EXISTS, managedChannel.toString());
            }

            int rows = mapper.insert(managedChannel);
            session.commit();
            return rows;
        }
    }

    /**
     * Update an existing managed channel.
     *
     * @param managedChannel the managed channel to be updated
     * @throws ManagedChannelException if managed channel does not exist
     */
    @Override
    public synchronized int update(final ManagedChannel managedChannel) throws ManagedChannelException {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelMapper mapper = session.getMapper(ManagedChannelMapper.class);
            long matchingChannels = mapper.count(c -> c.where(managedChannelId, isEqualTo(managedChannel.getManagedChannelId())));

            if (matchingChannels == 0) {
                throw new ManagedChannelException(UNKNOWN_CHANNEL, managedChannel.toString());
            }

            int rows = mapper.updateByPrimaryKey(managedChannel);
            session.commit();
            return rows;
        }
    }

    /**
     * Delete a managed channel.
     *
     * @param managedChannel the managed channel to be deleted
     * @throws ManagedChannelException if managed channel does not exist
     */
    @Override
    public synchronized int delete(final ManagedChannel managedChannel) throws ManagedChannelException {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelMapper mapper = session.getMapper(ManagedChannelMapper.class);
            long matchingChannels = mapper.count(c -> c.where(managedChannelId, isEqualTo(managedChannel.getManagedChannelId())));

            if (matchingChannels == 0) {
                throw new ManagedChannelException(UNKNOWN_CHANNEL, managedChannel.toString());
            }

            int rows = mapper.deleteByPrimaryKey(managedChannel.getManagedChannelId());
            session.commit();
            return rows;
        }
    }

    /**
     * Get all managed channels.
     *
     * @return a list of managed channels
     */
    @Override
    public synchronized List<ManagedChannel> getAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelMapper mapper = session.getMapper(ManagedChannelMapper.class);
            return mapper.select(SelectDSLCompleter.allRows());
        }
    }

    /**
     * Get a total count of all managed channels.
     *
     * @return number of total managed channels
     */
    public synchronized long count() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelMapper mapper = session.getMapper(ManagedChannelMapper.class);
            return mapper.count(CountDSLCompleter.allRows());
        }
    }
}
