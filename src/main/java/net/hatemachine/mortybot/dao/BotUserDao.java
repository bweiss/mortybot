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
import net.hatemachine.mortybot.model.BotUser;
import net.hatemachine.mortybot.util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.List;
import java.util.Optional;

import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;

public class BotUserDao {

    private final SqlSessionFactory sqlSessionFactory;

    public BotUserDao() {
        this.sqlSessionFactory = MyBatisUtil.getSqlSessionFactory();
    }

    public synchronized BotUser create(BotUser botUser) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BotUserMapper mapper = session.getMapper(BotUserMapper.class);
            mapper.insert(botUser);
            session.commit();
            return botUser;
        }
    }

    public synchronized List<BotUser> batchCreate(List<BotUser> botUsers) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BotUserMapper mapper = session.getMapper(BotUserMapper.class);
            mapper.insertMultiple(botUsers);
            session.commit();
            return botUsers;
        }
    }

    public synchronized boolean createIfNotExist(BotUser BotUser) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BotUserCustomMapper mapper = session.getMapper(BotUserCustomMapper.class);
            int count = mapper.ignoreInsert(BotUser);
            session.commit();
            return count > 0;
        }
    }

    public synchronized int batchCreateIfNotExist(List<BotUser> BotUsers) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BotUserCustomMapper mapper = session.getMapper(BotUserCustomMapper.class);
            session.commit();
            return mapper.ignoreInsertMultiple(BotUsers);
        }
    }

    public synchronized BotUser update(BotUser botUser) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BotUserMapper mapper = session.getMapper(BotUserMapper.class);
            mapper.updateByPrimaryKey(botUser);
            session.commit();
            return botUser;
        }
    }

    public synchronized void delete(int id) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BotUserMapper mapper = session.getMapper(BotUserMapper.class);
            mapper.deleteByPrimaryKey(id);
            session.commit();
        }
    }

    public synchronized void deleteWithName(String name) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BotUserMapper mapper = session.getMapper(BotUserMapper.class);
            mapper.delete(c -> c.where(BotUserDynamicSqlSupport.name, isEqualTo(name)));
            session.commit();
        }
    }

    public synchronized Optional<BotUser> get(Integer id) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BotUserMapper mapper = session.getMapper(BotUserMapper.class);
            return mapper.leftJoinSelectOne(c -> c.where(BotUserDynamicSqlSupport.id, isEqualTo(id)));
        }
    }

    public synchronized Optional<BotUser> getWithName(String name) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BotUserMapper mapper = session.getMapper(BotUserMapper.class);
            return mapper.leftJoinSelectOne(c -> c.where(BotUserDynamicSqlSupport.name, isEqualTo(name)));
        }
    }

    public synchronized List<BotUser> getAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BotUserMapper mapper = session.getMapper(BotUserMapper.class);
            return mapper.leftJoinSelect(c -> c);
        }
    }

    public synchronized Long count() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BotUserMapper mapper = session.getMapper(BotUserMapper.class);
            return mapper.count(c -> c);
        }
    }
}
