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

import net.hatemachine.mortybot.config.BotDefaults;
import net.hatemachine.mortybot.config.BotProperties;
import net.hatemachine.mortybot.model.BotChannel;
import net.hatemachine.mortybot.model.BotUser;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    private static SessionFactory sessionFactory;

    private HibernateUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static SessionFactory buildSessionFactory() {
        BotProperties props = BotProperties.getBotProperties();
        return new Configuration()
                .setProperty("hibernate.connection.url", props.getStringProperty("db.url", BotDefaults.DB_URL))
                .setProperty("hibernate.dialect", props.getStringProperty("hibernate.dialect"))
                .setProperty("hibernate.connection.driver_class", props.getStringProperty("db.driver"))
                .setProperty("hibernate.connection.provider_class", props.getStringProperty("hibernate.connection.provider_class"))
                .setProperty("hibernate.show_sql", props.getStringProperty("hibernate.show_sql", "false"))
                .setProperty("hibernate.hbm2ddl.auto", "none")
                .addAnnotatedClass(BotUser.class)
                .addAnnotatedClass(BotChannel.class)
                .buildSessionFactory();
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            sessionFactory = buildSessionFactory();
        }
        return sessionFactory;
    }
}
