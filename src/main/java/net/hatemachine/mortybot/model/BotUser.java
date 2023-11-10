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

import jakarta.persistence.*;
import org.hibernate.annotations.NaturalId;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;
import java.util.Set;

@Entity
public class BotUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NaturalId
    @Column(unique = true, nullable = false)
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> hostmasks;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> autoOpChannels;

    private String location;
    private String password;

    private boolean adminFlag = false;
    private boolean dccFlag = true;
    private boolean ignoreFlag = false;

    public BotUser() {}

    public BotUser(String name) {
        this.name = name;
    }

    public BotUser(String name, String hostmask) {
        this.name = name;
        this.hostmasks = Set.of(hostmask);
    }

    public BotUser(String name, Set<String> hostmasks) {
        this.name = name;
        this.hostmasks = hostmasks;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getHostmasks() {
        return hostmasks;
    }

    public void setHostmasks(Set<String> hostmasks) {
        this.hostmasks = hostmasks;
    }

    public Set<String> getAutoOpChannels() {
        return autoOpChannels;
    }

    public void setAutoOpChannels(Set<String> autoOpChannels) {
        this.autoOpChannels = autoOpChannels;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean hasAdminFlag() {
        return adminFlag;
    }

    public void setAdminFlag(boolean adminFlag) {
        this.adminFlag = adminFlag;
    }

    public boolean hasDccFlag() {
        return dccFlag;
    }

    public void setDccFlag(boolean dccFlag) {
        this.dccFlag = dccFlag;
    }

    public boolean hasIgnoreFlag() {
        return ignoreFlag;
    }

    public void setIgnoreFlag(boolean ignoreFlag) {
        this.ignoreFlag = ignoreFlag;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (this.getClass() != o.getClass()) {
            return false;
        }
        Class<?> oEffectiveClass = o instanceof HibernateProxy hibernateProxy ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy hibernateProxy ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        BotUser botUser = (BotUser) o;
        return getId() != null && Objects.equals(getId(), botUser.getId())
                && getName() != null && Objects.equals(getName(), botUser.getName());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "BotUser{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", hostmasks=" + hostmasks +
                ", autoOpChannels=" + autoOpChannels +
                ", location='" + location + '\'' +
                ", password='" + password + '\'' +
                ", adminFlag=" + adminFlag +
                ", dccFlag=" + dccFlag +
                ", ignoreFlag=" + ignoreFlag +
                '}';
    }
}