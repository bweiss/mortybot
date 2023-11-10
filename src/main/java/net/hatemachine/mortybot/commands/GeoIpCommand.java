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
package net.hatemachine.mortybot.commands;

import com.maxmind.geoip2.WebServiceClient;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.exception.AuthenticationException;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.exception.InvalidRequestException;
import com.maxmind.geoip2.exception.OutOfQueriesException;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Subdivision;
import net.hatemachine.mortybot.Command;
import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.config.BotProperties;
import net.hatemachine.mortybot.listeners.CommandListener;
import org.pircbotx.Colors;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

/**
 * Implements the GEOIP command. This performs a GeoIP2 (GeoLite2) lookup on an IP address.
 * Note that this requires a free GeoLite2 account and license key.
 *
 * <a href="https://dev.maxmind.com/geoip/geolite2-free-geolocation-data">https://dev.maxmind.com/geoip/geolite2-free-geolocation-data</a>
 */
@BotCommand(name = "GEOIP", help = {
        "Shows the geographic location of an IP address or hostname",
        "Usage: GEOIP <IP|hostname>"
})
public class GeoIpCommand implements Command {

    private static final String WEB_SERVICE_HOST = "geolite.info";

    private static final Logger log = LoggerFactory.getLogger(GeoIpCommand.class);

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    public GeoIpCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() {
        if (args.isEmpty()) {
            throw new IllegalArgumentException("Not enough arguments");
        }

        var botProperties = BotProperties.getBotProperties();
        var accountId = botProperties.getIntProperty("maxmind.account.id", Integer.parseInt(System.getenv("MAXMIND_ACCOUNT_ID")));
        var licenseKey = botProperties.getStringProperty("maxmind.license.key", System.getenv("MAXMIND_LICENSE_KEY"));
        var address = args.get(0);

        try (WebServiceClient client = new WebServiceClient.Builder(accountId, licenseKey)
                .host(WEB_SERVICE_HOST)
                .build()) {

            InetAddress ipAddress = InetAddress.getByName(address);
            CityResponse response = client.city(ipAddress);
            Country country = response.getCountry();
            Subdivision subdivision = response.getMostSpecificSubdivision();
            City city = response.getCity();

            event.respondWith(String.format("[%s] %s, %s (%s)",
                    Colors.BOLD + args.get(0) + Colors.BOLD,
                    city.getName(),
                    subdivision.getIsoCode(),
                    country.getName()));

        } catch (AddressNotFoundException e) {
            String errMsg = "Address not found";
            log.error("{}: {}", errMsg, address);
            event.respondWith(errMsg);

        } catch (AuthenticationException e) {
            String errMsg = "Authentication failed";
            log.error("{}: {}", errMsg, WEB_SERVICE_HOST, e);
            event.respondWith(errMsg);

        } catch (InvalidRequestException e) {
            String errMsg = "Invalid request";
            log.error(errMsg, e);
            event.respondWith(errMsg);

        } catch (OutOfQueriesException e) {
            String errMsg = "Query limit reached";
            log.error(errMsg);
            event.respondWith(errMsg);

        } catch (GeoIp2Exception | IOException e) {
            log.error("Exception encountered while looking up host", e);
            event.respondWith("Error");
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
