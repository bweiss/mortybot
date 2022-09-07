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
package net.hatemachine.mortybot.custom.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.hatemachine.mortybot.custom.entity.BotUserFlag;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BotUserFlagListHandler extends BaseTypeHandler<List<BotUserFlag>> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<BotUserFlag> botUserFlags, JdbcType jdbcType) throws SQLException {
        ps.setString(i, list2string(botUserFlags));
    }

    @Override
    public List<BotUserFlag> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String r = rs.getString(columnName);
        if (rs.wasNull())
            return null;
        return string2list(r);
    }

    @Override
    public List<BotUserFlag> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String r = rs.getString(columnIndex);
        if (rs.wasNull()) {
            return null;
        }
        return string2list(r);
    }

    @Override
    public List<BotUserFlag> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String r = cs.getString(columnIndex);
        if (cs.wasNull()) {
            return new ArrayList<>();
        }
        return string2list(r);
    }

    private String list2string(List<BotUserFlag> list) throws SQLException {
        try {
            if (list == null || list.isEmpty()) {
                return null;
            }
            return objectMapper.writeValueAsString(list);
        } catch (Exception ex) {
            throw new SQLException("Error when serializing bot user flags", ex);
        }
    }

    private List<BotUserFlag> string2list(String str) throws SQLException {
        try {
            if (str == null || str.isEmpty()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(str, new TypeReference<>() {

            });
        } catch (Exception ex) {
            throw new SQLException("Error when deserializing bot user flags", ex);
        }
    }
}
