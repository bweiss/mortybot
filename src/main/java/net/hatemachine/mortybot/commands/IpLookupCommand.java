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

import com.maxmind.geoip2.WebServiceClient;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Subdivision;
import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.config.BotState;
import net.hatemachine.mortybot.listeners.CommandListener;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

public class IpLookupCommand implements BotCommand {

    private static final String WEB_SERVICE_HOST = "geolite.info";

    private static final Logger log = LoggerFactory.getLogger(IpLookupCommand.class);

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
        if (args.isEmpty())
            throw new IllegalArgumentException("Not enough arguments");

        var bs = BotState.getBotState();
        var accountId = bs.getIntProperty("IpLookupCommand.accountId", Integer.parseInt(System.getenv("MAXMIND_ACCOUNT_ID")));
        var licenseKey = bs.getStringProperty("IpLookupCommand.licenseKey", System.getenv("MAXMIND_LICENSE_KEY"));

        WebServiceClient client = new WebServiceClient.Builder(accountId, licenseKey)
                .host(WEB_SERVICE_HOST)
                .build();

        try {
            InetAddress ipAddress = InetAddress.getByName(args.get(0));
            CityResponse response = client.city(ipAddress);
            Country country = response.getCountry();
            Subdivision subdivision = response.getMostSpecificSubdivision();
            City city = response.getCity();

            event.respondWith(String.format("[%s] %s, %s (%s)",
                    args.get(0), city.getName(), subdivision.getIsoCode(), country.getName()));

        } catch (IOException | GeoIp2Exception e) {
            log.error("Exception encountered while looking up host", e);
            event.respondWith(e.getMessage());
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
