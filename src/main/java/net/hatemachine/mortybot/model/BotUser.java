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
package net.hatemachine.mortybot.model;

import com.google.common.collect.Sets;

import javax.annotation.Generated;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import static net.hatemachine.mortybot.util.StringUtils.wildcardToRegex;

public class BotUser {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.6085291-04:00", comments="Source field: bot_users.bot_user_id")
    private Integer botUserId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.6115289-04:00", comments="Source field: bot_users.username")
    private String username;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.6115289-04:00", comments="Source field: bot_users.hostmasks")
    private String hostmasks;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.6115289-04:00", comments="Source field: bot_users.flags")
    private String flags;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.6115289-04:00", comments="Source field: bot_users.location")
    private String location;

    public BotUser(String username, String hostmasks, String flags) {
        this.username = username;
        this.hostmasks = hostmasks;
        this.flags = flags;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.6115289-04:00", comments="Source field: bot_users.bot_user_id")
    public Integer getBotUserId() {
        return botUserId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.6115289-04:00", comments="Source field: bot_users.bot_user_id")
    public void setBotUserId(Integer botUserId) {
        this.botUserId = botUserId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.6115289-04:00", comments="Source field: bot_users.username")
    public String getUsername() {
        return username;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.6115289-04:00", comments="Source field: bot_users.username")
    public void setUsername(String username) {
        this.username = username;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.6115289-04:00", comments="Source field: bot_users.hostmasks")
    public String getHostmasks() {
        return hostmasks;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.6115289-04:00", comments="Source field: bot_users.hostmasks")
    public void setHostmasks(String hostmasks) {
        this.hostmasks = hostmasks;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.6115289-04:00", comments="Source field: bot_users.flags")
    public String getFlags() {
        return flags;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.6115289-04:00", comments="Source field: bot_users.flags")
    public void setFlags(String flags) {
        this.flags = flags;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.6115289-04:00", comments="Source field: bot_users.location")
    public String getLocation() {
        return location;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.6115289-04:00", comments="Source field: bot_users.location")
    public void setLocation(String location) {
        this.location = location;
    }

    public boolean hasMatchingHostmask(String userhost) {
        return Arrays.stream(hostmasks.split(",")).anyMatch(h -> Pattern.matches(wildcardToRegex(h), userhost));
    }

    public boolean hasFlag(String flag) {
        return Arrays.asList(flags.split(",")).contains(flag);
    }

    public void addHostmask(String hostmask) {
        Set<String> hostmaskSet = Sets.newHashSet(Arrays.asList(hostmasks.split(",")));
        hostmaskSet.add(hostmask);
        hostmasks = String.join(",", hostmaskSet);
    }

    public void removeHostmask(String hostmask) {
        Set<String> hostmaskSet = Sets.newHashSet(Arrays.asList(hostmasks.split(",")));
        hostmaskSet.remove(hostmask);
        hostmasks = String.join(",", hostmaskSet);
    }

    public void addFlag(String flag) {
        Set<String> flagSet = Sets.newHashSet(Arrays.asList(flags.split(",")));
        flagSet.add(flag);
        flags = String.join(",", flagSet);
    }

    public void removeFlag(String hostmask) {
        Set<String> flagSet = Sets.newHashSet(Arrays.asList(flags.split(",")));
        flagSet.remove(hostmask);
        flags = String.join(",", flagSet);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BotUser botUser = (BotUser) o;
        return Objects.equals(botUserId, botUser.botUserId) && Objects.equals(username, botUser.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(botUserId, username);
    }

    @Override
    public String toString() {
        return "BotUser{" +
                "botUserId=" + botUserId +
                ", username='" + username + '\'' +
                ", hostmasks='" + hostmasks + '\'' +
                ", flags='" + flags + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}