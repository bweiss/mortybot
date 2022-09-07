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
import net.hatemachine.mortybot.model.ManagedChannel;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.insert.render.MultiRowInsertStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;

import java.util.Collection;
import java.util.List;

import static net.hatemachine.mortybot.mapper.ManagedChannelDynamicSqlSupport.*;

public interface ManagedChannelCustomMapper {

    @InsertProvider(type= SqlProviderAdapter.class, method="insert")
    @Options(useGeneratedKeys=true,keyProperty="record.id")
    int ignoreInsert(InsertStatementProvider<ManagedChannel> insertStatement);

    @Insert({
            "${insertStatement}"
    })
    @Options(useGeneratedKeys=true,keyProperty="records.id")
    int ignoreInsertMultiple(@Param("insertStatement") String insertStatement, @Param("records") List<ManagedChannel> records);

    default int ignoreInsertMultiple(MultiRowInsertStatementProvider<ManagedChannel> multipleInsertStatement) {
        return ignoreInsertMultiple(multipleInsertStatement.getInsertStatement(), multipleInsertStatement.getRecords());
    }

    default int ignoreInsert(ManagedChannel record) {
        return MyBatis3CustomUtils.ignoreInsert(this::ignoreInsert, record, managedChannel, c ->
                c.map(name).toProperty("name")
                        .map(managedChannelFlags).toProperty("managedChannelFlags")
        );
    }

    default int ignoreInsertMultiple(Collection<ManagedChannel> records) {
        return MyBatis3CustomUtils.ignoreInsertMultiple(this::ignoreInsertMultiple, records, managedChannel, c ->
                c.map(name).toProperty("name")
                        .map(managedChannelFlags).toProperty("managedChannelFlags")
        );
    }
}