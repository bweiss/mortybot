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

import com.catyee.generator.utils.MyBatis3CustomUtils;
import net.hatemachine.mortybot.custom.mapper.ManagedChannelCustomMapper;
import net.hatemachine.mortybot.mapper.ManagedChannelDynamicSqlSupport;
import net.hatemachine.mortybot.mapper.ManagedChannelMapper;
import net.hatemachine.mortybot.model.ManagedChannel;
import net.hatemachine.mortybot.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.List;

import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;

public class ManagedChannelDao {

    private final SqlSessionFactory sqlSessionFactory;

    public ManagedChannelDao() {
        this.sqlSessionFactory = MyBatisUtil.getSqlSessionFactory();
    }

    public ManagedChannel create(ManagedChannel managedChannel) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelMapper mapper = session.getMapper(ManagedChannelMapper.class);
            mapper.insert(managedChannel);
            session.commit();
            return managedChannel;
        }
    }

    public List<ManagedChannel> batchCreate(List<ManagedChannel> ManagedChannels) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelMapper mapper = session.getMapper(ManagedChannelMapper.class);
            mapper.insertMultiple(ManagedChannels);
            session.commit();
            return ManagedChannels;
        }
    }

    public boolean createIfNotExist(ManagedChannel ManagedChannel) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelCustomMapper mapper = session.getMapper(ManagedChannelCustomMapper.class);
            int count = mapper.ignoreInsert(ManagedChannel);
            session.commit();
            return count > 0;
        }
    }

    public int batchCreateIfNotExist(List<ManagedChannel> ManagedChannels) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelCustomMapper mapper = session.getMapper(ManagedChannelCustomMapper.class);
            session.commit();
            return mapper.ignoreInsertMultiple(ManagedChannels);
        }
    }

    public ManagedChannel update(ManagedChannel managedChannel) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelMapper mapper = session.getMapper(ManagedChannelMapper.class);
            mapper.updateByPrimaryKey(managedChannel);
            session.commit();
            return managedChannel;
        }
    }

    public void delete(int id) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelMapper mapper = session.getMapper(ManagedChannelMapper.class);
            mapper.deleteByPrimaryKey(id);
            session.commit();
        }
    }

    public void deleteWithName(String name) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelMapper mapper = session.getMapper(ManagedChannelMapper.class);
            mapper.delete(c -> c.where(ManagedChannelDynamicSqlSupport.name, isEqualTo(name)));
            session.commit();
        }
    }

    public ManagedChannel get(Integer id) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelMapper mapper = session.getMapper(ManagedChannelMapper.class);
            return mapper.selectByPrimaryKey(id).orElse(null);
        }
    }

    public List<ManagedChannel> getWithName(String name) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelMapper mapper = session.getMapper(ManagedChannelMapper.class);
            return mapper.select(c -> c.where(ManagedChannelDynamicSqlSupport.name, isEqualTo(name)));
        }
    }

    public List<ManagedChannel> getAll() {
        return getAll(null, null);
    }

    public List<ManagedChannel> getAll(Long limit, Long offset) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelMapper mapper = session.getMapper(ManagedChannelMapper.class);
            return mapper.select(c -> MyBatis3CustomUtils.buildLimitOffset(c, limit, offset));
        }
    }
}
