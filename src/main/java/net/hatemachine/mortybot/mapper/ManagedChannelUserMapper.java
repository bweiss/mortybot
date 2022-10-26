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

import static net.hatemachine.mortybot.mapper.BotUserDynamicSqlSupport.botUser;
import static net.hatemachine.mortybot.mapper.ManagedChannelDynamicSqlSupport.managedChannel;
import static net.hatemachine.mortybot.mapper.ManagedChannelUserDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import com.catyee.generator.entity.JoinDetail;
import com.catyee.generator.utils.MyBatis3CustomUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.hatemachine.mortybot.custom.handler.ManagedChannelUserFlagListHandler;
import net.hatemachine.mortybot.model.ManagedChannelUser;
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
public interface ManagedChannelUserMapper {
    BasicColumn[] selectList = BasicColumn.columnList(id, managedChannelId, botUserId, managedChannelUserFlags);

    BasicColumn[] leftJoinSelectList = BasicColumn.columnList(id, managedChannelId, botUserId, managedChannelUserFlags,
		(managedChannel.id).as("managed_channel_id"), 
		(managedChannel.name).as("managed_channel_name"), 
		(managedChannel.managedChannelFlags).as("managed_channel_managed_channel_flags"), 
		(managedChannel.bans).as("managed_channel_bans"), 
		(managedChannel.modes).as("managed_channel_modes"), 
		(botUser.id).as("bot_user_id"), 
		(botUser.name).as("bot_user_name"), 
		(botUser.botUserHostmasks).as("bot_user_bot_user_hostmasks"), 
		(botUser.botUserFlags).as("bot_user_bot_user_flags"), 
		(botUser.location).as("bot_user_location"));

    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    long count(SelectStatementProvider selectStatement);

    @DeleteProvider(type=SqlProviderAdapter.class, method="delete")
    int delete(DeleteStatementProvider deleteStatement);

    @InsertProvider(type=SqlProviderAdapter.class, method="insert")
    @Options(useGeneratedKeys=true,keyProperty="record.id")
    int insert(InsertStatementProvider<ManagedChannelUser> insertStatement);

    @Insert({
        "${insertStatement}"
    })
    @Options(useGeneratedKeys=true,keyProperty="records.id")
    int insertMultiple(@Param("insertStatement") String insertStatement, @Param("records") List<ManagedChannelUser> records);

    default int insertMultiple(MultiRowInsertStatementProvider<ManagedChannelUser> multipleInsertStatement) {
        return insertMultiple(multipleInsertStatement.getInsertStatement(), multipleInsertStatement.getRecords());
    }

    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("ManagedChannelUserResult")
    Optional<ManagedChannelUser> selectOne(SelectStatementProvider selectStatement);

    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="ManagedChannelUserResult", value = {
        @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="managed_channel_id", property="managedChannelId", jdbcType=JdbcType.INTEGER),
        @Result(column="bot_user_id", property="botUserId", jdbcType=JdbcType.INTEGER),
        @Result(column="managed_channel_user_flags", property="managedChannelUserFlags", typeHandler=ManagedChannelUserFlagListHandler.class, jdbcType=JdbcType.VARCHAR)
    })
    List<ManagedChannelUser> selectMany(SelectStatementProvider selectStatement);

    @UpdateProvider(type=SqlProviderAdapter.class, method="update")
    int update(UpdateStatementProvider updateStatement);

    default long count(CountDSLCompleter completer) {
        return MyBatis3Utils.countFrom(this::count, managedChannelUser, completer);
    }

    default int delete(DeleteDSLCompleter completer) {
        return MyBatis3Utils.deleteFrom(this::delete, managedChannelUser, completer);
    }

    default int deleteByPrimaryKey(Integer id_) {
        return delete(c -> 
            c.where(id, isEqualTo(id_))
        );
    }

    default int insert(ManagedChannelUser record) {
        return MyBatis3Utils.insert(this::insert, record, managedChannelUser, c ->
            c.map(managedChannelId).toProperty("managedChannelId")
            .map(botUserId).toProperty("botUserId")
            .map(managedChannelUserFlags).toProperty("managedChannelUserFlags")
        );
    }

    default int insertMultiple(Collection<ManagedChannelUser> records) {
        return MyBatis3Utils.insertMultiple(this::insertMultiple, records, managedChannelUser, c ->
            c.map(managedChannelId).toProperty("managedChannelId")
            .map(botUserId).toProperty("botUserId")
            .map(managedChannelUserFlags).toProperty("managedChannelUserFlags")
        );
    }

    default int insertSelective(ManagedChannelUser record) {
        return MyBatis3Utils.insert(this::insert, record, managedChannelUser, c ->
            c.map(managedChannelId).toPropertyWhenPresent("managedChannelId", record::getManagedChannelId)
            .map(botUserId).toPropertyWhenPresent("botUserId", record::getBotUserId)
            .map(managedChannelUserFlags).toPropertyWhenPresent("managedChannelUserFlags", record::getManagedChannelUserFlags)
        );
    }

    default Optional<ManagedChannelUser> selectOne(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectOne(this::selectOne, selectList, managedChannelUser, completer);
    }

    default List<ManagedChannelUser> select(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectList(this::selectMany, selectList, managedChannelUser, completer);
    }

    default List<ManagedChannelUser> selectDistinct(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectDistinct(this::selectMany, selectList, managedChannelUser, completer);
    }

    default Optional<ManagedChannelUser> selectByPrimaryKey(Integer id_) {
        return selectOne(c ->
            c.where(id, isEqualTo(id_))
        );
    }

    default int update(UpdateDSLCompleter completer) {
        return MyBatis3Utils.update(this::update, managedChannelUser, completer);
    }

    static UpdateDSL<UpdateModel> updateAllColumns(ManagedChannelUser record, UpdateDSL<UpdateModel> dsl) {
        return dsl.set(managedChannelId).equalTo(record::getManagedChannelId)
                .set(botUserId).equalTo(record::getBotUserId)
                .set(managedChannelUserFlags).equalTo(record::getManagedChannelUserFlags);
    }

    static UpdateDSL<UpdateModel> updateSelectiveColumns(ManagedChannelUser record, UpdateDSL<UpdateModel> dsl) {
        return dsl.set(managedChannelId).equalToWhenPresent(record::getManagedChannelId)
                .set(botUserId).equalToWhenPresent(record::getBotUserId)
                .set(managedChannelUserFlags).equalToWhenPresent(record::getManagedChannelUserFlags);
    }

    default int updateByPrimaryKey(ManagedChannelUser record) {
        return update(c ->
            c.set(managedChannelId).equalTo(record::getManagedChannelId)
            .set(botUserId).equalTo(record::getBotUserId)
            .set(managedChannelUserFlags).equalTo(record::getManagedChannelUserFlags)
            .where(id, isEqualTo(record::getId))
        );
    }

    default int updateByPrimaryKeySelective(ManagedChannelUser record) {
        return update(c ->
            c.set(managedChannelId).equalToWhenPresent(record::getManagedChannelId)
            .set(botUserId).equalToWhenPresent(record::getBotUserId)
            .set(managedChannelUserFlags).equalToWhenPresent(record::getManagedChannelUserFlags)
            .where(id, isEqualTo(record::getId))
        );
    }

    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("JoinManagedChannelUserResult")
    Optional<ManagedChannelUser> leftJoinSelectOne(SelectStatementProvider selectStatement);

    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("JoinManagedChannelUserResult")
    List<ManagedChannelUser> leftJoinSelectMany(SelectStatementProvider selectStatement);

    default List<ManagedChannelUser> leftJoinSelect(SelectDSLCompleter completer) {
        List<JoinDetail> joinDetails = new ArrayList<>();
        joinDetails.add(JoinDetail.of(managedChannelId, managedChannel, managedChannel.id));
        joinDetails.add(JoinDetail.of(botUserId, botUser, botUser.id));
        return MyBatis3CustomUtils.leftJoinSelectList(this::leftJoinSelectMany, leftJoinSelectList, managedChannelUser, joinDetails , completer);
    }

    default Optional<ManagedChannelUser> leftJoinSelectOne(SelectDSLCompleter completer) {
        List<JoinDetail> joinDetails = new ArrayList<>();
        joinDetails.add(JoinDetail.of(managedChannelId, managedChannel, managedChannel.id));
        joinDetails.add(JoinDetail.of(botUserId, botUser, botUser.id));
        return MyBatis3CustomUtils.leftJoinSelectOne(this::leftJoinSelectOne, leftJoinSelectList, managedChannelUser, joinDetails , completer);
    }
}