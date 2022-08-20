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

import static net.hatemachine.mortybot.mapper.ManagedChannelDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.annotation.Generated;
import net.hatemachine.mortybot.model.ManagedChannel;
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
public interface ManagedChannelMapper extends CommonCountMapper, CommonDeleteMapper, CommonInsertMapper<ManagedChannel>, CommonUpdateMapper {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.247447-04:00", comments="Source Table: managed_channels")
    BasicColumn[] selectList = BasicColumn.columnList(managedChannelId, name, autoJoinFlag, modes, enforceModesFlag);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.247099-04:00", comments="Source Table: managed_channels")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="ManagedChannelResult", value = {
        @Result(column="managed_channel_id", property="managedChannelId", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="name", property="name", jdbcType=JdbcType.VARCHAR),
        @Result(column="auto_join_flag", property="autoJoinFlag", jdbcType=JdbcType.INTEGER),
        @Result(column="modes", property="modes", jdbcType=JdbcType.VARCHAR),
        @Result(column="enforce_modes_flag", property="enforceModesFlag", jdbcType=JdbcType.INTEGER)
    })
    List<ManagedChannel> selectMany(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.247154-04:00", comments="Source Table: managed_channels")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("ManagedChannelResult")
    Optional<ManagedChannel> selectOne(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.247189-04:00", comments="Source Table: managed_channels")
    default long count(CountDSLCompleter completer) {
        return MyBatis3Utils.countFrom(this::count, managedChannel, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.247215-04:00", comments="Source Table: managed_channels")
    default int delete(DeleteDSLCompleter completer) {
        return MyBatis3Utils.deleteFrom(this::delete, managedChannel, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.247237-04:00", comments="Source Table: managed_channels")
    default int deleteByPrimaryKey(Integer managedChannelId_) {
        return delete(c -> 
            c.where(managedChannelId, isEqualTo(managedChannelId_))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.24726-04:00", comments="Source Table: managed_channels")
    default int insert(ManagedChannel row) {
        return MyBatis3Utils.insert(this::insert, row, managedChannel, c ->
            c.map(managedChannelId).toProperty("managedChannelId")
            .map(name).toProperty("name")
            .map(autoJoinFlag).toProperty("autoJoinFlag")
            .map(modes).toProperty("modes")
            .map(enforceModesFlag).toProperty("enforceModesFlag")
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.247313-04:00", comments="Source Table: managed_channels")
    default int insertMultiple(Collection<ManagedChannel> records) {
        return MyBatis3Utils.insertMultiple(this::insertMultiple, records, managedChannel, c ->
            c.map(managedChannelId).toProperty("managedChannelId")
            .map(name).toProperty("name")
            .map(autoJoinFlag).toProperty("autoJoinFlag")
            .map(modes).toProperty("modes")
            .map(enforceModesFlag).toProperty("enforceModesFlag")
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.24737-04:00", comments="Source Table: managed_channels")
    default int insertSelective(ManagedChannel row) {
        return MyBatis3Utils.insert(this::insert, row, managedChannel, c ->
            c.map(managedChannelId).toPropertyWhenPresent("managedChannelId", row::getManagedChannelId)
            .map(name).toPropertyWhenPresent("name", row::getName)
            .map(autoJoinFlag).toPropertyWhenPresent("autoJoinFlag", row::getAutoJoinFlag)
            .map(modes).toPropertyWhenPresent("modes", row::getModes)
            .map(enforceModesFlag).toPropertyWhenPresent("enforceModesFlag", row::getEnforceModesFlag)
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.247475-04:00", comments="Source Table: managed_channels")
    default Optional<ManagedChannel> selectOne(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectOne(this::selectOne, selectList, managedChannel, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.247504-04:00", comments="Source Table: managed_channels")
    default List<ManagedChannel> select(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectList(this::selectMany, selectList, managedChannel, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.247532-04:00", comments="Source Table: managed_channels")
    default List<ManagedChannel> selectDistinct(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectDistinct(this::selectMany, selectList, managedChannel, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.247557-04:00", comments="Source Table: managed_channels")
    default Optional<ManagedChannel> selectByPrimaryKey(Integer managedChannelId_) {
        return selectOne(c ->
            c.where(managedChannelId, isEqualTo(managedChannelId_))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.247584-04:00", comments="Source Table: managed_channels")
    default int update(UpdateDSLCompleter completer) {
        return MyBatis3Utils.update(this::update, managedChannel, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.247615-04:00", comments="Source Table: managed_channels")
    static UpdateDSL<UpdateModel> updateAllColumns(ManagedChannel row, UpdateDSL<UpdateModel> dsl) {
        return dsl.set(managedChannelId).equalTo(row::getManagedChannelId)
                .set(name).equalTo(row::getName)
                .set(autoJoinFlag).equalTo(row::getAutoJoinFlag)
                .set(modes).equalTo(row::getModes)
                .set(enforceModesFlag).equalTo(row::getEnforceModesFlag);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.247664-04:00", comments="Source Table: managed_channels")
    static UpdateDSL<UpdateModel> updateSelectiveColumns(ManagedChannel row, UpdateDSL<UpdateModel> dsl) {
        return dsl.set(managedChannelId).equalToWhenPresent(row::getManagedChannelId)
                .set(name).equalToWhenPresent(row::getName)
                .set(autoJoinFlag).equalToWhenPresent(row::getAutoJoinFlag)
                .set(modes).equalToWhenPresent(row::getModes)
                .set(enforceModesFlag).equalToWhenPresent(row::getEnforceModesFlag);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.247722-04:00", comments="Source Table: managed_channels")
    default int updateByPrimaryKey(ManagedChannel row) {
        return update(c ->
            c.set(name).equalTo(row::getName)
            .set(autoJoinFlag).equalTo(row::getAutoJoinFlag)
            .set(modes).equalTo(row::getModes)
            .set(enforceModesFlag).equalTo(row::getEnforceModesFlag)
            .where(managedChannelId, isEqualTo(row::getManagedChannelId))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.247777-04:00", comments="Source Table: managed_channels")
    default int updateByPrimaryKeySelective(ManagedChannel row) {
        return update(c ->
            c.set(name).equalToWhenPresent(row::getName)
            .set(autoJoinFlag).equalToWhenPresent(row::getAutoJoinFlag)
            .set(modes).equalToWhenPresent(row::getModes)
            .set(enforceModesFlag).equalToWhenPresent(row::getEnforceModesFlag)
            .where(managedChannelId, isEqualTo(row::getManagedChannelId))
        );
    }
}