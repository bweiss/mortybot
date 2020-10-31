package net.hatemachine.mortybot.commands;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import net.hatemachine.mortybot.BotCommand;
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

    public IpLookupCommand(GenericMessageEvent event) {
        this.event = event;
    }

    @Override
    public void execute(List<String> args) {
        File database = null;
        DatabaseReader reader = null;
        CityResponse response = null;
        InetAddress ipAddress = null;
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
                Country country = response.getCountry();
                City city = response.getCity();
                event.respondWith(ipAddress + " -> " + city.getName() + ", " + country.getIsoCode());
            }
        }
        catch (UnknownHostException e) {
            String errMsg = "Unknown host";
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
}
