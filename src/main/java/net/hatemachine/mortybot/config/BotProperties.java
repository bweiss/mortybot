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
package net.hatemachine.mortybot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class BotProperties {

    private static final Logger log = LoggerFactory.getLogger(BotProperties.class);

    private static BotProperties botProperties = null;

    private final Properties properties;

    private BotProperties() {
        String pathSeparator = FileSystems.getDefault().getSeparator();
        Path propertiesFile = Path.of(getBotConfigDir() + pathSeparator + BotDefaults.PROPERTIES_FILE);
        properties = new Properties();

        try (BufferedReader reader = Files.newBufferedReader(propertiesFile)) {
            properties.load(reader);
        } catch (FileNotFoundException e) {
            log.warn("File not found: {}", propertiesFile);
        } catch (IOException e) {
            log.error("Unable to read bot properties file: {}", propertiesFile, e);
        }
    }

    public static BotProperties getBotProperties() {
        if (botProperties == null) {
            botProperties = new BotProperties();
        }

        return botProperties;
    }

    public String getBotHome() {
        String home = System.getenv("MORTYBOT_HOME");
        String home2 = System.getProperty("user.dir");
        return home == null ? home2 : home;
    }

    public Path getBotConfigDir() {
        String prop = System.getProperty("mortybot.config.dir");
        return Path.of(prop != null ? prop : "conf");
    }

    public Properties getAll() {
        return properties;
    }

    public synchronized String getStringProperty(String name) {
        return properties.getProperty(name);
    }

    public synchronized String getStringProperty(String name, String defaultValue) {
        String prop = getStringProperty(name);
        return prop == null ? defaultValue : prop;
    }

    public synchronized void setStringProperty(String name, String newValue) {
        properties.setProperty(name, newValue);
    }

    public synchronized boolean getBooleanProperty(String name, boolean defaultValue) {
        String prop = getStringProperty(name);
        return prop == null ? defaultValue : prop.equalsIgnoreCase("true");
    }

    public synchronized void setBooleanProperty(String name, boolean newValue) {
        setStringProperty(name, newValue ? "true" : "false");
    }

    public synchronized int getIntProperty(String name, int defaultValue) {
        String prop = getStringProperty(name);
        return prop == null ? defaultValue : Integer.parseInt(prop);
    }

    public synchronized void setIntProperty(String name, int newValue) {
        setStringProperty(name, Integer.toString(newValue));
    }

    public synchronized float getFloatProperty(String name, float defaultValue) {
        String prop = getStringProperty(name);
        return prop == null ? defaultValue : Float.parseFloat(prop);
    }

    public synchronized void setFloatProperty(String name, float newValue) {
        setStringProperty(name, Float.toString(newValue));
    }
}
