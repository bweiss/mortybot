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
package net.hatemachine.mortybot.mapper;

import static net.hatemachine.mortybot.mapper.BotUserDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.annotation.Generated;
import net.hatemachine.mortybot.model.BotUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.delete.DeleteDSLCompleter;
import org.mybatis.dynamic.sql.select.CountDSLCompleter;
import org.mybatis.dynamic.sql.select.SelectDSLCompleter;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.UpdateDSL;
import org.mybatis.dynamic.sql.update.UpdateDSLCompleter;
import org.mybatis.dynamic.sql.update.UpdateModel;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;
import org.mybatis.dynamic.sql.util.mybatis3.CommonCountMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonDeleteMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonInsertMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonUpdateMapper;
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3Utils;

@Mapper
public interface BotUserMapper extends CommonCountMapper, CommonDeleteMapper, CommonInsertMapper<BotUser>, CommonUpdateMapper {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.6250905-04:00", comments="Source Table: bot_users")
    BasicColumn[] selectList = BasicColumn.columnList(botUserId, username, hostmasks, flags, location);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.6155288-04:00", comments="Source Table: bot_users")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="BotUserResult", value = {
        @Result(column="bot_user_id", property="botUserId", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="username", property="username", jdbcType=JdbcType.VARCHAR),
        @Result(column="hostmasks", property="hostmasks", jdbcType=JdbcType.VARCHAR),
        @Result(column="flags", property="flags", jdbcType=JdbcType.VARCHAR),
        @Result(column="location", property="location", jdbcType=JdbcType.VARCHAR)
    })
    List<BotUser> selectMany(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.6180813-04:00", comments="Source Table: bot_users")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("BotUserResult")
    Optional<BotUser> selectOne(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.6180813-04:00", comments="Source Table: bot_users")
    default long count(CountDSLCompleter completer) {
        return MyBatis3Utils.countFrom(this::count, botUser, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.61909-04:00", comments="Source Table: bot_users")
    default int delete(DeleteDSLCompleter completer) {
        return MyBatis3Utils.deleteFrom(this::delete, botUser, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.61909-04:00", comments="Source Table: bot_users")
    default int deleteByPrimaryKey(Integer botUserId_) {
        return delete(c -> 
            c.where(botUserId, isEqualTo(botUserId_))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.6200909-04:00", comments="Source Table: bot_users")
    default int insert(BotUser row) {
        return MyBatis3Utils.insert(this::insert, row, botUser, c ->
            c.map(botUserId).toProperty("botUserId")
            .map(username).toProperty("username")
            .map(hostmasks).toProperty("hostmasks")
            .map(flags).toProperty("flags")
            .map(location).toProperty("location")
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.6220907-04:00", comments="Source Table: bot_users")
    default int insertMultiple(Collection<BotUser> records) {
        return MyBatis3Utils.insertMultiple(this::insertMultiple, records, botUser, c ->
            c.map(botUserId).toProperty("botUserId")
            .map(username).toProperty("username")
            .map(hostmasks).toProperty("hostmasks")
            .map(flags).toProperty("flags")
            .map(location).toProperty("location")
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.6230901-04:00", comments="Source Table: bot_users")
    default int insertSelective(BotUser row) {
        return MyBatis3Utils.insert(this::insert, row, botUser, c ->
            c.map(botUserId).toPropertyWhenPresent("botUserId", row::getBotUserId)
            .map(username).toPropertyWhenPresent("username", row::getUsername)
            .map(hostmasks).toPropertyWhenPresent("hostmasks", row::getHostmasks)
            .map(flags).toPropertyWhenPresent("flags", row::getFlags)
            .map(location).toPropertyWhenPresent("location", row::getLocation)
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.6260899-04:00", comments="Source Table: bot_users")
    default Optional<BotUser> selectOne(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectOne(this::selectOne, selectList, botUser, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.6260899-04:00", comments="Source Table: bot_users")
    default List<BotUser> select(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectList(this::selectMany, selectList, botUser, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.62709-04:00", comments="Source Table: bot_users")
    default List<BotUser> selectDistinct(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectDistinct(this::selectMany, selectList, botUser, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.62709-04:00", comments="Source Table: bot_users")
    default Optional<BotUser> selectByPrimaryKey(Integer botUserId_) {
        return selectOne(c ->
            c.where(botUserId, isEqualTo(botUserId_))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.62709-04:00", comments="Source Table: bot_users")
    default int update(UpdateDSLCompleter completer) {
        return MyBatis3Utils.update(this::update, botUser, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.6280897-04:00", comments="Source Table: bot_users")
    static UpdateDSL<UpdateModel> updateAllColumns(BotUser row, UpdateDSL<UpdateModel> dsl) {
        return dsl.set(botUserId).equalTo(row::getBotUserId)
                .set(username).equalTo(row::getUsername)
                .set(hostmasks).equalTo(row::getHostmasks)
                .set(flags).equalTo(row::getFlags)
                .set(location).equalTo(row::getLocation);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.62909-04:00", comments="Source Table: bot_users")
    static UpdateDSL<UpdateModel> updateSelectiveColumns(BotUser row, UpdateDSL<UpdateModel> dsl) {
        return dsl.set(botUserId).equalToWhenPresent(row::getBotUserId)
                .set(username).equalToWhenPresent(row::getUsername)
                .set(hostmasks).equalToWhenPresent(row::getHostmasks)
                .set(flags).equalToWhenPresent(row::getFlags)
                .set(location).equalToWhenPresent(row::getLocation);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.62909-04:00", comments="Source Table: bot_users")
    default int updateByPrimaryKey(BotUser row) {
        return update(c ->
            c.set(username).equalTo(row::getUsername)
            .set(hostmasks).equalTo(row::getHostmasks)
            .set(flags).equalTo(row::getFlags)
            .set(location).equalTo(row::getLocation)
            .where(botUserId, isEqualTo(row::getBotUserId))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.6300898-04:00", comments="Source Table: bot_users")
    default int updateByPrimaryKeySelective(BotUser row) {
        return update(c ->
            c.set(username).equalToWhenPresent(row::getUsername)
            .set(hostmasks).equalToWhenPresent(row::getHostmasks)
            .set(flags).equalToWhenPresent(row::getFlags)
            .set(location).equalToWhenPresent(row::getLocation)
            .where(botUserId, isEqualTo(row::getBotUserId))
        );
    }
}