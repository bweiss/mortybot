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
package net.hatemachine.mortybot.custom.mapper;

import com.catyee.generator.utils.MyBatis3CustomUtils;
import net.hatemachine.mortybot.model.BotUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;

import java.util.Collection;
import java.util.List;

import static net.hatemachine.mortybot.mapper.BotUserDynamicSqlSupport.*;

public interface BotUserCustomMapper {

    @InsertProvider(type= SqlProviderAdapter.class, method="insert")
    @Options(useGeneratedKeys=true,keyProperty="record.id")
    int ignoreInsert(InsertStatementProvider<BotUser> insertStatement);

    @Insert({
            "${insertStatement}"
    })
    @Options(useGeneratedKeys=true,keyProperty="records.id")
    int ignoreInsertMultiple(@Param("insertStatement") String insertStatement, @Param("records") List<BotUser> records);

    default int ignoreInsertMultiple(MultiRowInsertStatementProvider<BotUser> multipleInsertStatement) {
        return ignoreInsertMultiple(multipleInsertStatement.getInsertStatement(), multipleInsertStatement.getRecords());
    }
    
    default int ignoreInsert(BotUser record) {
        return MyBatis3CustomUtils.ignoreInsert(this::ignoreInsert, record, botUser, c ->
                c.map(name).toProperty("name")
                        .map(botUserFlags).toProperty("botUserFlags")
                        .map(botUserHostmasks).toProperty("botUserHostmasks")
                        .map(location).toProperty("location")
        );
    }
    
    default int ignoreInsertMultiple(Collection<BotUser> records) {
        return MyBatis3CustomUtils.ignoreInsertMultiple(this::ignoreInsertMultiple, records, botUser, c ->
                c.map(name).toProperty("name")
                        .map(botUserFlags).toProperty("botUserFlags")
                        .map(botUserHostmasks).toProperty("botUserHostmasks")
                        .map(location).toProperty("location")
        );
    }
}
