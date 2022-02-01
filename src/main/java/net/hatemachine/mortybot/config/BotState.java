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

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class BotState {

    private static final Logger log = LoggerFactory.getLogger(BotState.class);

    private static BotState botState = null;

    private final Properties state;

    public BotState() {
        var path = Path.of(this.getBotHome() + "/conf/" + BotDefaults.PROPERTIES_FILE);
        state = new Properties();

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            state.load(reader);
        } catch (FileNotFoundException e) {
           log.warn("Bot properties file not found, falling back to defaults");
        } catch (IOException e) {
            log.error("Exception encountered reading bot properties", e);
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

    public String getStringProperty(String name) {
        return state.getProperty(name);
    }

    public String getStringProperty(String name, String defaultValue) {
        String prop = getStringProperty(name);
        return prop == null ? defaultValue : prop;
    }

    public void setStringProperty(String name, String newValue) {
        state.setProperty(name, newValue);
    }

    public boolean getBooleanProperty(String name, boolean defaultValue) {
        String prop = getStringProperty(name);
        return prop == null ? defaultValue : prop.equalsIgnoreCase("true");
    }

    public void setBooleanProperty(String name, boolean newValue) {
        setStringProperty(name, newValue ? "true" : "false");
    }

    public int getIntProperty(String name, int defaultValue) {
        String prop = getStringProperty(name);
        return prop == null ? defaultValue : Integer.parseInt(prop);
    }

    public void setIntProperty(String name, int newValue) {
        setStringProperty(name, Integer.toString(newValue));
    }

    public float getFloatProperty(String name, float defaultValue) {
        String prop = getStringProperty(name);
        return prop == null ? defaultValue : Float.parseFloat(prop);
    }

    public void setFloatProperty(String name, float newValue) {
        setStringProperty(name, Float.toString(newValue));
    }

    public void save() {
        // TODO: would like to implement a way to save state but have to think about this more first
        throw new NotImplementedException("method not implemented yet");

        // implementation 1
//        var path = Path.of(getBotHome() + "/conf/" + BotDefaults.PROPERTIES_FILE);
//        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
//            state.store(writer, "Bot Configuration");
//        } catch (IOException e) {
//            log.error("Exception encountered saving state", e);
//        }

        // implementation 2 using FileSaver
//        try {
//            var path = Path.of(getBotHome() + "/conf/" + BotDefaults.PROPERTIES_FILE);
//            FileSaver saver = new FileSaver(path);
//            Writer writer = saver.getWriter();
//            PrintWriter out = new PrintWriter(writer);
//            state.store(out, "Bot Properties");
//            out.close();
//            saver.finish();
//        } catch (IOException e) {
//            log.error("Exception encountered writing file", e);
//        }
    }
}
