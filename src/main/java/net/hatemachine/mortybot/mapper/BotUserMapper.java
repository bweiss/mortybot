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
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T12:33:00.9810598-04:00", comments="Source Table: bot_users")
    BasicColumn[] selectList = BasicColumn.columnList(botUserId, username, hostmasks, flags);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T12:33:00.9514346-04:00", comments="Source Table: bot_users")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="BotUserResult", value = {
        @Result(column="bot_user_id", property="botUserId", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="username", property="username", jdbcType=JdbcType.VARCHAR),
        @Result(column="hostmasks", property="hostmasks", jdbcType=JdbcType.VARCHAR),
        @Result(column="flags", property="flags", jdbcType=JdbcType.VARCHAR)
    })
    List<BotUser> selectMany(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T12:33:00.9574013-04:00", comments="Source Table: bot_users")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("BotUserResult")
    Optional<BotUser> selectOne(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T12:33:00.9584235-04:00", comments="Source Table: bot_users")
    default long count(CountDSLCompleter completer) {
        return MyBatis3Utils.countFrom(this::count, botUser, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T12:33:00.9593942-04:00", comments="Source Table: bot_users")
    default int delete(DeleteDSLCompleter completer) {
        return MyBatis3Utils.deleteFrom(this::delete, botUser, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T12:33:00.9663988-04:00", comments="Source Table: bot_users")
    default int deleteByPrimaryKey(Integer botUserId_) {
        return delete(c -> 
            c.where(botUserId, isEqualTo(botUserId_))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T12:33:00.969156-04:00", comments="Source Table: bot_users")
    default int insert(BotUser row) {
        return MyBatis3Utils.insert(this::insert, row, botUser, c ->
            c.map(botUserId).toProperty("botUserId")
            .map(username).toProperty("username")
            .map(hostmasks).toProperty("hostmasks")
            .map(flags).toProperty("flags")
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T12:33:00.972924-04:00", comments="Source Table: bot_users")
    default int insertMultiple(Collection<BotUser> records) {
        return MyBatis3Utils.insertMultiple(this::insertMultiple, records, botUser, c ->
            c.map(botUserId).toProperty("botUserId")
            .map(username).toProperty("username")
            .map(hostmasks).toProperty("hostmasks")
            .map(flags).toProperty("flags")
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T12:33:00.9766512-04:00", comments="Source Table: bot_users")
    default int insertSelective(BotUser row) {
        return MyBatis3Utils.insert(this::insert, row, botUser, c ->
            c.map(botUserId).toPropertyWhenPresent("botUserId", row::getBotUserId)
            .map(username).toPropertyWhenPresent("username", row::getUsername)
            .map(hostmasks).toPropertyWhenPresent("hostmasks", row::getHostmasks)
            .map(flags).toPropertyWhenPresent("flags", row::getFlags)
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T12:33:00.9820545-04:00", comments="Source Table: bot_users")
    default Optional<BotUser> selectOne(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectOne(this::selectOne, selectList, botUser, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T12:33:00.9832561-04:00", comments="Source Table: bot_users")
    default List<BotUser> select(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectList(this::selectMany, selectList, botUser, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T12:33:00.983771-04:00", comments="Source Table: bot_users")
    default List<BotUser> selectDistinct(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectDistinct(this::selectMany, selectList, botUser, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T12:33:00.9857869-04:00", comments="Source Table: bot_users")
    default Optional<BotUser> selectByPrimaryKey(Integer botUserId_) {
        return selectOne(c ->
            c.where(botUserId, isEqualTo(botUserId_))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T12:33:00.9867899-04:00", comments="Source Table: bot_users")
    default int update(UpdateDSLCompleter completer) {
        return MyBatis3Utils.update(this::update, botUser, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T12:33:00.9877899-04:00", comments="Source Table: bot_users")
    static UpdateDSL<UpdateModel> updateAllColumns(BotUser row, UpdateDSL<UpdateModel> dsl) {
        return dsl.set(botUserId).equalTo(row::getBotUserId)
                .set(username).equalTo(row::getUsername)
                .set(hostmasks).equalTo(row::getHostmasks)
                .set(flags).equalTo(row::getFlags);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T12:33:00.9907881-04:00", comments="Source Table: bot_users")
    static UpdateDSL<UpdateModel> updateSelectiveColumns(BotUser row, UpdateDSL<UpdateModel> dsl) {
        return dsl.set(botUserId).equalToWhenPresent(row::getBotUserId)
                .set(username).equalToWhenPresent(row::getUsername)
                .set(hostmasks).equalToWhenPresent(row::getHostmasks)
                .set(flags).equalToWhenPresent(row::getFlags);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T12:33:00.9937946-04:00", comments="Source Table: bot_users")
    default int updateByPrimaryKey(BotUser row) {
        return update(c ->
            c.set(username).equalTo(row::getUsername)
            .set(hostmasks).equalTo(row::getHostmasks)
            .set(flags).equalTo(row::getFlags)
            .where(botUserId, isEqualTo(row::getBotUserId))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T12:33:00.9977866-04:00", comments="Source Table: bot_users")
    default int updateByPrimaryKeySelective(BotUser row) {
        return update(c ->
            c.set(username).equalToWhenPresent(row::getUsername)
            .set(hostmasks).equalToWhenPresent(row::getHostmasks)
            .set(flags).equalToWhenPresent(row::getFlags)
            .where(botUserId, isEqualTo(row::getBotUserId))
        );
    }
}