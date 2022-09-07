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
package net.hatemachine.mortybot.util;

import net.hatemachine.mortybot.config.BotProperties;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.migration.JavaMigrationLoader;
import org.apache.ibatis.migration.JdbcConnectionProvider;
import org.apache.ibatis.migration.operations.UpOperation;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class MyBatisUtil {

    private static final Logger log = LoggerFactory.getLogger(MyBatisUtil.class);

    private static SqlSessionFactory sqlSessionFactory;

    static {
        BotProperties props = BotProperties.getBotProperties();
        String configFile = "mybatis-config.xml";

        // perform any pending database migration operations
        new UpOperation().operate(
                new JdbcConnectionProvider(props.getStringProperty("db.driver"), props.getStringProperty("db.url"), null, null),
                new JavaMigrationLoader("net.hatemachine.mortybot.migration"), null, null
        );

        try (InputStream is = Resources.getResourceAsStream(configFile)) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(is, props.getAll());
        } catch (IOException ex) {
            log.error("Failed to create SQL session factory", ex);
        }
    }

    private MyBatisUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static SqlSessionFactory getSqlSessionFactory() {
        return sqlSessionFactory;
    }
}
