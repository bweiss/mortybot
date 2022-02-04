/*
 * MortyBot - An IRC bot built on the PircBotX framework.
 * Copyright © 2022 Brian Weiss (brianmweiss@gmail.com)
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

import com.darwinsys.io.FileSaver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

public class BotState {

    private static final Logger log = LoggerFactory.getLogger(BotState.class);

    private static BotState botState = null;

    private final Properties state;

    public BotState() {
        Path path = Path.of(this.getBotHome() + "/conf/" + BotDefaults.PROPERTIES_FILE);
        state = new Properties();

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            state.load(reader);
        } catch (FileNotFoundException e) {
            log.warn("File not found: {}", path);
        } catch (IOException e) {
            log.error("Unable to read bot properties file: {}", path, e);
        }
    }

    public static BotState getBotState() {
        if (botState == null) {
            botState = new BotState();
        }

        return botState;
    }

    public String getBotHome() {
        String home = System.getenv("MORTYBOT_HOME");
        String home2 = System.getProperty("user.dir");
        return home == null ? home2 : home;
    }

    public Properties getProperties() {
        return state;
    }

    public synchronized String getStringProperty(String name) {
        return state.getProperty(name);
    }

    public synchronized String getStringProperty(String name, String defaultValue) {
        String prop = getStringProperty(name);
        return prop == null ? defaultValue : prop;
    }

    public synchronized void setStringProperty(String name, String newValue) {
        state.setProperty(name, newValue);
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

    public synchronized void save() {
        Path path = Path.of(getBotHome() + "/conf/" + BotDefaults.PROPERTIES_FILE);

        try {
            FileSaver saver = new FileSaver(path);
            Writer writer = saver.getWriter();
            PrintWriter out = new PrintWriter(writer);

            out.println("""
                    #
                    # Bot properties
                    #
                    # Note that this file will be overwritten by the bot during runtime if a property is changed.
                    # It should not be edited directly while the bot is running.
                    #
                    """);

            // transfer into a TreeMap for sorting purposes
            Map<String, String> sortedMap = new TreeMap<>();
            state.forEach((k, v) -> sortedMap.put(k.toString(), v.toString()));

            // write all properties to our file
            sortedMap.forEach((k, v) -> out.println(k + "=" + v));

            out.close();
            saver.finish();

        } catch (IOException e) {
            log.error("Unable to write bot properties file: {}", path, e);
        }
    }
}
