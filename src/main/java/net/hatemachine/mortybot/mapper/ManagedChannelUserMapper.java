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

import static net.hatemachine.mortybot.mapper.ManagedChannelUserDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.annotation.Generated;
import net.hatemachine.mortybot.model.ManagedChannelUser;
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
public interface ManagedChannelUserMapper extends CommonCountMapper, CommonDeleteMapper, CommonInsertMapper<ManagedChannelUser>, CommonUpdateMapper {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.248698-04:00", comments="Source Table: managed_channel_users")
    BasicColumn[] selectList = BasicColumn.columnList(managedChannelUserId, managedChannelId, botUserId, autoOpFlag, autoVoiceFlag);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.248349-04:00", comments="Source Table: managed_channel_users")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="ManagedChannelUserResult", value = {
        @Result(column="managed_channel_user_id", property="managedChannelUserId", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="managed_channel_id", property="managedChannelId", jdbcType=JdbcType.INTEGER),
        @Result(column="bot_user_id", property="botUserId", jdbcType=JdbcType.INTEGER),
        @Result(column="auto_op_flag", property="autoOpFlag", jdbcType=JdbcType.INTEGER),
        @Result(column="auto_voice_flag", property="autoVoiceFlag", jdbcType=JdbcType.INTEGER)
    })
    List<ManagedChannelUser> selectMany(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.248411-04:00", comments="Source Table: managed_channel_users")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("ManagedChannelUserResult")
    Optional<ManagedChannelUser> selectOne(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.248442-04:00", comments="Source Table: managed_channel_users")
    default long count(CountDSLCompleter completer) {
        return MyBatis3Utils.countFrom(this::count, managedChannelUser, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.248467-04:00", comments="Source Table: managed_channel_users")
    default int delete(DeleteDSLCompleter completer) {
        return MyBatis3Utils.deleteFrom(this::delete, managedChannelUser, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.248489-04:00", comments="Source Table: managed_channel_users")
    default int deleteByPrimaryKey(Integer managedChannelUserId_) {
        return delete(c -> 
            c.where(managedChannelUserId, isEqualTo(managedChannelUserId_))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.248514-04:00", comments="Source Table: managed_channel_users")
    default int insert(ManagedChannelUser row) {
        return MyBatis3Utils.insert(this::insert, row, managedChannelUser, c ->
            c.map(managedChannelUserId).toProperty("managedChannelUserId")
            .map(managedChannelId).toProperty("managedChannelId")
            .map(botUserId).toProperty("botUserId")
            .map(autoOpFlag).toProperty("autoOpFlag")
            .map(autoVoiceFlag).toProperty("autoVoiceFlag")
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.248572-04:00", comments="Source Table: managed_channel_users")
    default int insertMultiple(Collection<ManagedChannelUser> records) {
        return MyBatis3Utils.insertMultiple(this::insertMultiple, records, managedChannelUser, c ->
            c.map(managedChannelUserId).toProperty("managedChannelUserId")
            .map(managedChannelId).toProperty("managedChannelId")
            .map(botUserId).toProperty("botUserId")
            .map(autoOpFlag).toProperty("autoOpFlag")
            .map(autoVoiceFlag).toProperty("autoVoiceFlag")
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.248628-04:00", comments="Source Table: managed_channel_users")
    default int insertSelective(ManagedChannelUser row) {
        return MyBatis3Utils.insert(this::insert, row, managedChannelUser, c ->
            c.map(managedChannelUserId).toPropertyWhenPresent("managedChannelUserId", row::getManagedChannelUserId)
            .map(managedChannelId).toPropertyWhenPresent("managedChannelId", row::getManagedChannelId)
            .map(botUserId).toPropertyWhenPresent("botUserId", row::getBotUserId)
            .map(autoOpFlag).toPropertyWhenPresent("autoOpFlag", row::getAutoOpFlag)
            .map(autoVoiceFlag).toPropertyWhenPresent("autoVoiceFlag", row::getAutoVoiceFlag)
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.248725-04:00", comments="Source Table: managed_channel_users")
    default Optional<ManagedChannelUser> selectOne(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectOne(this::selectOne, selectList, managedChannelUser, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.248753-04:00", comments="Source Table: managed_channel_users")
    default List<ManagedChannelUser> select(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectList(this::selectMany, selectList, managedChannelUser, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.248779-04:00", comments="Source Table: managed_channel_users")
    default List<ManagedChannelUser> selectDistinct(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectDistinct(this::selectMany, selectList, managedChannelUser, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.2488-04:00", comments="Source Table: managed_channel_users")
    default Optional<ManagedChannelUser> selectByPrimaryKey(Integer managedChannelUserId_) {
        return selectOne(c ->
            c.where(managedChannelUserId, isEqualTo(managedChannelUserId_))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.248825-04:00", comments="Source Table: managed_channel_users")
    default int update(UpdateDSLCompleter completer) {
        return MyBatis3Utils.update(this::update, managedChannelUser, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.248847-04:00", comments="Source Table: managed_channel_users")
    static UpdateDSL<UpdateModel> updateAllColumns(ManagedChannelUser row, UpdateDSL<UpdateModel> dsl) {
        return dsl.set(managedChannelUserId).equalTo(row::getManagedChannelUserId)
                .set(managedChannelId).equalTo(row::getManagedChannelId)
                .set(botUserId).equalTo(row::getBotUserId)
                .set(autoOpFlag).equalTo(row::getAutoOpFlag)
                .set(autoVoiceFlag).equalTo(row::getAutoVoiceFlag);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.248892-04:00", comments="Source Table: managed_channel_users")
    static UpdateDSL<UpdateModel> updateSelectiveColumns(ManagedChannelUser row, UpdateDSL<UpdateModel> dsl) {
        return dsl.set(managedChannelUserId).equalToWhenPresent(row::getManagedChannelUserId)
                .set(managedChannelId).equalToWhenPresent(row::getManagedChannelId)
                .set(botUserId).equalToWhenPresent(row::getBotUserId)
                .set(autoOpFlag).equalToWhenPresent(row::getAutoOpFlag)
                .set(autoVoiceFlag).equalToWhenPresent(row::getAutoVoiceFlag);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.248946-04:00", comments="Source Table: managed_channel_users")
    default int updateByPrimaryKey(ManagedChannelUser row) {
        return update(c ->
            c.set(managedChannelId).equalTo(row::getManagedChannelId)
            .set(botUserId).equalTo(row::getBotUserId)
            .set(autoOpFlag).equalTo(row::getAutoOpFlag)
            .set(autoVoiceFlag).equalTo(row::getAutoVoiceFlag)
            .where(managedChannelUserId, isEqualTo(row::getManagedChannelUserId))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.248998-04:00", comments="Source Table: managed_channel_users")
    default int updateByPrimaryKeySelective(ManagedChannelUser row) {
        return update(c ->
            c.set(managedChannelId).equalToWhenPresent(row::getManagedChannelId)
            .set(botUserId).equalToWhenPresent(row::getBotUserId)
            .set(autoOpFlag).equalToWhenPresent(row::getAutoOpFlag)
            .set(autoVoiceFlag).equalToWhenPresent(row::getAutoVoiceFlag)
            .where(managedChannelUserId, isEqualTo(row::getManagedChannelUserId))
        );
    }
}