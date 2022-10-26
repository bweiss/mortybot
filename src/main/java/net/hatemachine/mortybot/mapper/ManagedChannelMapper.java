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
import static net.hatemachine.mortybot.mapper.ManagedChannelUserDynamicSqlSupport.managedChannelUser;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import com.catyee.generator.entity.JoinDetail;
import com.catyee.generator.utils.MyBatis3CustomUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.hatemachine.mortybot.custom.handler.ManagedChannelFlagListHandler;
import net.hatemachine.mortybot.custom.handler.StringListHandler;
import net.hatemachine.mortybot.model.ManagedChannel;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.delete.DeleteDSLCompleter;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider;
import org.mybatis.dynamic.sql.select.CountDSLCompleter;
import org.mybatis.dynamic.sql.select.SelectDSLCompleter;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.UpdateDSL;
import org.mybatis.dynamic.sql.update.UpdateDSLCompleter;
import org.mybatis.dynamic.sql.update.UpdateModel;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3Utils;

@Mapper
public interface ManagedChannelMapper {
    BasicColumn[] selectList = BasicColumn.columnList(id, name, managedChannelFlags, bans, modes);

    BasicColumn[] leftJoinSelectList = BasicColumn.columnList(id, name, managedChannelFlags, bans, modes,
		(managedChannelUser.id).as("managed_channel_user_id"), 
		(managedChannelUser.managedChannelId).as("managed_channel_user_managed_channel_id"), 
		(managedChannelUser.botUserId).as("managed_channel_user_bot_user_id"), 
		(managedChannelUser.managedChannelUserFlags).as("managed_channel_user_managed_channel_user_flags"));

    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    long count(SelectStatementProvider selectStatement);

    @DeleteProvider(type=SqlProviderAdapter.class, method="delete")
    int delete(DeleteStatementProvider deleteStatement);

    @InsertProvider(type=SqlProviderAdapter.class, method="insert")
    @Options(useGeneratedKeys=true,keyProperty="record.id")
    int insert(InsertStatementProvider<ManagedChannel> insertStatement);

    @Insert({
        "${insertStatement}"
    })
    @Options(useGeneratedKeys=true,keyProperty="records.id")
    int insertMultiple(@Param("insertStatement") String insertStatement, @Param("records") List<ManagedChannel> records);

    default int insertMultiple(MultiRowInsertStatementProvider<ManagedChannel> multipleInsertStatement) {
        return insertMultiple(multipleInsertStatement.getInsertStatement(), multipleInsertStatement.getRecords());
    }

    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("ManagedChannelResult")
    Optional<ManagedChannel> selectOne(SelectStatementProvider selectStatement);

    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="ManagedChannelResult", value = {
        @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="name", property="name", jdbcType=JdbcType.VARCHAR),
        @Result(column="managed_channel_flags", property="managedChannelFlags", typeHandler=ManagedChannelFlagListHandler.class, jdbcType=JdbcType.VARCHAR),
        @Result(column="bans", property="bans", typeHandler=StringListHandler.class, jdbcType=JdbcType.VARCHAR),
        @Result(column="modes", property="modes", jdbcType=JdbcType.VARCHAR)
    })
    List<ManagedChannel> selectMany(SelectStatementProvider selectStatement);

    @UpdateProvider(type=SqlProviderAdapter.class, method="update")
    int update(UpdateStatementProvider updateStatement);

    default long count(CountDSLCompleter completer) {
        return MyBatis3Utils.countFrom(this::count, managedChannel, completer);
    }

    default int delete(DeleteDSLCompleter completer) {
        return MyBatis3Utils.deleteFrom(this::delete, managedChannel, completer);
    }

    default int deleteByPrimaryKey(Integer id_) {
        return delete(c -> 
            c.where(id, isEqualTo(id_))
        );
    }

    default int insert(ManagedChannel record) {
        return MyBatis3Utils.insert(this::insert, record, managedChannel, c ->
            c.map(name).toProperty("name")
            .map(managedChannelFlags).toProperty("managedChannelFlags")
            .map(bans).toProperty("bans")
            .map(modes).toProperty("modes")
        );
    }

    default int insertMultiple(Collection<ManagedChannel> records) {
        return MyBatis3Utils.insertMultiple(this::insertMultiple, records, managedChannel, c ->
            c.map(name).toProperty("name")
            .map(managedChannelFlags).toProperty("managedChannelFlags")
            .map(bans).toProperty("bans")
            .map(modes).toProperty("modes")
        );
    }

    default int insertSelective(ManagedChannel record) {
        return MyBatis3Utils.insert(this::insert, record, managedChannel, c ->
            c.map(name).toPropertyWhenPresent("name", record::getName)
            .map(managedChannelFlags).toPropertyWhenPresent("managedChannelFlags", record::getManagedChannelFlags)
            .map(bans).toPropertyWhenPresent("bans", record::getBans)
            .map(modes).toPropertyWhenPresent("modes", record::getModes)
        );
    }

    default Optional<ManagedChannel> selectOne(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectOne(this::selectOne, selectList, managedChannel, completer);
    }

    default List<ManagedChannel> select(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectList(this::selectMany, selectList, managedChannel, completer);
    }

    default List<ManagedChannel> selectDistinct(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectDistinct(this::selectMany, selectList, managedChannel, completer);
    }

    default Optional<ManagedChannel> selectByPrimaryKey(Integer id_) {
        return selectOne(c ->
            c.where(id, isEqualTo(id_))
        );
    }

    default int update(UpdateDSLCompleter completer) {
        return MyBatis3Utils.update(this::update, managedChannel, completer);
    }

    static UpdateDSL<UpdateModel> updateAllColumns(ManagedChannel record, UpdateDSL<UpdateModel> dsl) {
        return dsl.set(name).equalTo(record::getName)
                .set(managedChannelFlags).equalTo(record::getManagedChannelFlags)
                .set(bans).equalTo(record::getBans)
                .set(modes).equalTo(record::getModes);
    }

    static UpdateDSL<UpdateModel> updateSelectiveColumns(ManagedChannel record, UpdateDSL<UpdateModel> dsl) {
        return dsl.set(name).equalToWhenPresent(record::getName)
                .set(managedChannelFlags).equalToWhenPresent(record::getManagedChannelFlags)
                .set(bans).equalToWhenPresent(record::getBans)
                .set(modes).equalToWhenPresent(record::getModes);
    }

    default int updateByPrimaryKey(ManagedChannel record) {
        return update(c ->
            c.set(name).equalTo(record::getName)
            .set(managedChannelFlags).equalTo(record::getManagedChannelFlags)
            .set(bans).equalTo(record::getBans)
            .set(modes).equalTo(record::getModes)
            .where(id, isEqualTo(record::getId))
        );
    }

    default int updateByPrimaryKeySelective(ManagedChannel record) {
        return update(c ->
            c.set(name).equalToWhenPresent(record::getName)
            .set(managedChannelFlags).equalToWhenPresent(record::getManagedChannelFlags)
            .set(bans).equalToWhenPresent(record::getBans)
            .set(modes).equalToWhenPresent(record::getModes)
            .where(id, isEqualTo(record::getId))
        );
    }

    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("JoinManagedChannelResult")
    Optional<ManagedChannel> leftJoinSelectOne(SelectStatementProvider selectStatement);

    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("JoinManagedChannelResult")
    List<ManagedChannel> leftJoinSelectMany(SelectStatementProvider selectStatement);

    default List<ManagedChannel> leftJoinSelect(SelectDSLCompleter completer) {
        List<JoinDetail> joinDetails = new ArrayList<>();
        joinDetails.add(JoinDetail.of(id, managedChannelUser, managedChannelUser.managedChannelId));
        return MyBatis3CustomUtils.leftJoinSelectList(this::leftJoinSelectMany, leftJoinSelectList, managedChannel, joinDetails , completer);
    }

    default Optional<ManagedChannel> leftJoinSelectOne(SelectDSLCompleter completer) {
        List<JoinDetail> joinDetails = new ArrayList<>();
        joinDetails.add(JoinDetail.of(id, managedChannelUser, managedChannelUser.managedChannelId));
        return MyBatis3CustomUtils.leftJoinSelectOne(this::leftJoinSelectOne, leftJoinSelectList, managedChannel, joinDetails , completer);
    }
}