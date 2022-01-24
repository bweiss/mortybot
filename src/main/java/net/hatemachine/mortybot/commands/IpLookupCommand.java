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
package net.hatemachine.mortybot.commands;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.listeners.CommandListener;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;

public class IpLookupCommand implements BotCommand {

    private static final Logger log = LoggerFactory.getLogger(IpLookupCommand.class);

    private static final String GEOLITE2_CITY_DB = "GeoLite2-City.mmdb";

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    public IpLookupCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() {
        File database;
        DatabaseReader reader;
        CityResponse response;
        InetAddress ipAddress;
        URL resource = IpLookupCommand.class.getClassLoader().getResource(GEOLITE2_CITY_DB);

        if (args.isEmpty()) {
            throw new IllegalArgumentException("No IP specified!");
        }

        if (resource == null) {
            log.error("Unable to find GeoLite2-City database: " + GEOLITE2_CITY_DB);
            return;
        }

        try {
            database = new File(resource.toURI());
            reader = new DatabaseReader.Builder(database).build();
            ipAddress = InetAddress.getByName(args.get(0));
            response = reader.city(ipAddress);
            if (response != null) {
                var country = response.getCountry();
                var city = response.getCity();
                event.respondWith(ipAddress + " -> " + city.getName() + ", " + country.getIsoCode());
            }
        }
        catch (UnknownHostException e) {
            var errMsg = "Unknown host";
            log.error("{}: {}", errMsg, args.get(0));
            event.respondWith(errMsg);
        }
        catch (URISyntaxException | IOException e) {
            log.error("Error reading from database: {}", e.getMessage());
        }
        catch (GeoIp2Exception e) {
            log.error("Unable to locate address: {}", e.getMessage());
        }
    }

    @Override
    public GenericMessageEvent getEvent() {
        return event;
    }

    @Override
    public CommandListener.CommandSource getSource() {
        return source;
    }

    @Override
    public List<String> getArgs() {
        return args;
    }
}
