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

import net.hatemachine.mortybot.custom.mapper.ManagedChannelCustomMapper;
import net.hatemachine.mortybot.mapper.ManagedChannelDynamicSqlSupport;
import net.hatemachine.mortybot.mapper.ManagedChannelMapper;
import net.hatemachine.mortybot.mapper.ManagedChannelUserDynamicSqlSupport;
import net.hatemachine.mortybot.mapper.ManagedChannelUserMapper;
import net.hatemachine.mortybot.model.ManagedChannel;
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
 * DAO class for interacting with managed channels.
 */
public class ManagedChannelDao {

    private final SqlSessionFactory sqlSessionFactory;

    public ManagedChannelDao() {
        this.sqlSessionFactory = MyBatisUtil.getSqlSessionFactory();
    }

    /**
     * Creates a new managed channel entry.
     *
     * @param managedChannel the managed channel to create
     * @return the managed channel that was created
     */
    public synchronized ManagedChannel create(ManagedChannel managedChannel) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelMapper mapper = session.getMapper(ManagedChannelMapper.class);
            mapper.insert(managedChannel);
            session.commit();
            return managedChannel;
        }
    }

    /**
     * Creates multiple new managed channels from a list.
     *
     * @param managedChannels the list of managed channels to create
     * @return the list of managed channels that were created
     */
    public synchronized List<ManagedChannel> batchCreate(List<ManagedChannel> managedChannels) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelMapper mapper = session.getMapper(ManagedChannelMapper.class);
            mapper.insertMultiple(managedChannels);
            session.commit();
            return managedChannels;
        }
    }

    /**
     * Creates a new managed channel if it does not yet exist.
     *
     * @param managedChannel the managed channel to create
     * @return true if the managed channel was created, or false if not
     */
    public synchronized boolean createIfNotExist(ManagedChannel managedChannel) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelCustomMapper mapper = session.getMapper(ManagedChannelCustomMapper.class);
            int count = mapper.ignoreInsert(managedChannel);
            session.commit();
            return count > 0;
        }
    }

    /**
     * Creates multiple new managed channels from a list if they do not yet exist.
     *
     * @param managedChannels the list of managed channels to create
     * @return the total count of managed channels created
     */
    public synchronized int batchCreateIfNotExist(List<ManagedChannel> managedChannels) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelCustomMapper mapper = session.getMapper(ManagedChannelCustomMapper.class);
            int count = mapper.ignoreInsertMultiple(managedChannels);
            session.commit();
            return count;
        }
    }

    /**
     * Updates the details of a managed channel.
     *
     * @param managedChannel the managed channel to update
     * @return the managed channel after being updated
     */
    public synchronized ManagedChannel update(ManagedChannel managedChannel) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelMapper mapper = session.getMapper(ManagedChannelMapper.class);
            mapper.updateByPrimaryKey(managedChannel);
            session.commit();
            return managedChannel;
        }
    }

    /**
     * Deletes a managed channel by ID, as well as any managed channel user entries for that channel.
     *
     * @param id the id of the managed channel to be deleted
     */
    public synchronized void delete(int id) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            // delete any managed channel user entries for this channel
            ManagedChannelUserMapper mcuMapper = session.getMapper(ManagedChannelUserMapper.class);
            DeleteStatementProvider deleteStatement = deleteFrom(ManagedChannelUserDynamicSqlSupport.managedChannelUser)
                    .where(ManagedChannelUserDynamicSqlSupport.managedChannelId, isEqualTo(id))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            mcuMapper.delete(deleteStatement);

            // delete the channel
            ManagedChannelMapper mapper = session.getMapper(ManagedChannelMapper.class);
            mapper.deleteByPrimaryKey(id);

            session.commit();
        }
    }

    /**
     * Retrieves a managed channel by ID.
     *
     * @param id the id of the managed channel to retrieve
     * @return an optional containing the managed channel, if it exists, or an empty optional if not
     */
    public synchronized Optional<ManagedChannel> get(Integer id) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelMapper mapper = session.getMapper(ManagedChannelMapper.class);
            return mapper.leftJoinSelectOne(c -> c.where(ManagedChannelDynamicSqlSupport.id, isEqualTo(id)));
        }
    }

    /**
     * Retrieves a managed channel by name.
     *
     * @param name the name of the managed channel to retrieve
     * @return an optional containing the managed channel, if it exists, or an empty optional if not
     */
    public synchronized Optional<ManagedChannel> getWithName(String name) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelMapper mapper = session.getMapper(ManagedChannelMapper.class);
            return mapper.leftJoinSelectOne(c -> c.where(ManagedChannelDynamicSqlSupport.name, isEqualTo(name)));
        }
    }

    /**
     * Retrieves all managed channels.
     *
     * @return a list of managed channels
     */
    public synchronized List<ManagedChannel> getAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelMapper mapper = session.getMapper(ManagedChannelMapper.class);
            return mapper.leftJoinSelect(c -> c);
        }
    }

    /**
     * Retrieves multiple managed channels from a list of IDs.
     *
     * @param idList the list of managed channel IDs
     * @return a list of managed channels
     */
    public synchronized List<ManagedChannel> getMultipleWithIdList(List<Integer> idList) {
        if (idList == null || idList.isEmpty()) {
            return new ArrayList<>();
        }
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelMapper mapper = session.getMapper(ManagedChannelMapper.class);
            return mapper.leftJoinSelect(c -> c.where(ManagedChannelDynamicSqlSupport.id, isIn(idList)));
        }
    }

    /**
     * Retrieves multiple managed channels from a list of names.
     *
     * @param nameList the list of managed channel names
     * @return a list of managed channels
     */
    public synchronized List<ManagedChannel> getMultipleWithNameList(List<String> nameList) {
        if (nameList == null || nameList.isEmpty()) {
            return new ArrayList<>();
        }
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelMapper mapper = session.getMapper(ManagedChannelMapper.class);
            return mapper.leftJoinSelect(c -> c.where(ManagedChannelDynamicSqlSupport.name, isIn(nameList)));
        }
    }
}
