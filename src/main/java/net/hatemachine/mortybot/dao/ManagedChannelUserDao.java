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

public class ManagedChannelUserDao {

    private final SqlSessionFactory sqlSessionFactory;

    public ManagedChannelUserDao() {
        this.sqlSessionFactory = MyBatisUtil.getSqlSessionFactory();
    }

    public synchronized ManagedChannelUser create(ManagedChannelUser managedChannelUser) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelUserMapper mapper = session.getMapper(ManagedChannelUserMapper.class);
            mapper.insert(managedChannelUser);
            session.commit();
            return managedChannelUser;
        }
    }

    public synchronized List<ManagedChannelUser> batchCreate(List<ManagedChannelUser> ManagedChannelUsers) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelUserMapper mapper = session.getMapper(ManagedChannelUserMapper.class);
            mapper.insertMultiple(ManagedChannelUsers);
            session.commit();
            return ManagedChannelUsers;
        }
    }

    public synchronized boolean createIfNotExist(ManagedChannelUser ManagedChannelUser) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelUserCustomMapper mapper = session.getMapper(ManagedChannelUserCustomMapper.class);
            int count = mapper.ignoreInsert(ManagedChannelUser);
            session.commit();
            return count > 0;
        }
    }

    public synchronized int batchCreateIfNotExist(List<ManagedChannelUser> ManagedChannelUsers) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelUserCustomMapper mapper = session.getMapper(ManagedChannelUserCustomMapper.class);
            session.commit();
            return mapper.ignoreInsertMultiple(ManagedChannelUsers);
        }
    }

    public synchronized ManagedChannelUser update(ManagedChannelUser managedChannelUser) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelUserMapper mapper = session.getMapper(ManagedChannelUserMapper.class);
            mapper.updateByPrimaryKey(managedChannelUser);
            session.commit();
            return managedChannelUser;
        }
    }

    public synchronized void delete(int id) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelUserMapper mapper = session.getMapper(ManagedChannelUserMapper.class);
            mapper.deleteByPrimaryKey(id);
            session.commit();
        }
    }

    public synchronized Optional<ManagedChannelUser> get(Integer id) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelUserMapper mapper = session.getMapper(ManagedChannelUserMapper.class);
            return mapper.leftJoinSelectOne(c -> c.where(ManagedChannelUserDynamicSqlSupport.id, isEqualTo(id)));
        }
    }

    public synchronized List<ManagedChannelUser> getWithManagedChannelId(Integer managedChannelId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelUserMapper mapper = session.getMapper(ManagedChannelUserMapper.class);
            return mapper.leftJoinSelect(c -> c.where(ManagedChannelUserDynamicSqlSupport.managedChannelId, isEqualTo(managedChannelId)));
        }
    }

    public synchronized Optional<ManagedChannelUser> getWithManagedChannelIdAndBotUserId(Integer managedChannelId, Integer botUserId) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelUserMapper mapper = session.getMapper(ManagedChannelUserMapper.class);
            return mapper.leftJoinSelectOne(c -> c.where(ManagedChannelUserDynamicSqlSupport.managedChannelId, isEqualTo(managedChannelId))
                    .and(ManagedChannelUserDynamicSqlSupport.botUserId, isEqualTo(botUserId)));
        }
    }

    public synchronized List<ManagedChannelUser> getAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ManagedChannelUserMapper mapper = session.getMapper(ManagedChannelUserMapper.class);
            return mapper.leftJoinSelect(c -> c);
        }
    }
}
