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

@Entity
public class BotChannel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NaturalId
    @Column(unique = true, nullable = false)
    private String name;

    private boolean autoJoinFlag = true;
    private boolean shortenLinksFlag = true;
    private boolean showLinkTitlesFlag = true;

    public BotChannel() {}

    public BotChannel(String name) {
        this.name = name;
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

    public boolean hasAutoJoinFlag() {
        return autoJoinFlag;
    }

    public void setAutoJoinFlag(boolean autoJoinFlag) {
        this.autoJoinFlag = autoJoinFlag;
    }

    public boolean hasShortenLinksFlag() {
        return shortenLinksFlag;
    }

    public void setShortenLinksFlag(boolean shortenLinksFlag) {
        this.shortenLinksFlag = shortenLinksFlag;
    }

    public boolean hasShowLinkTitlesFlag() {
        return showLinkTitlesFlag;
    }

    public void setShowLinkTitlesFlag(boolean showLinkTitlesFlag) {
        this.showLinkTitlesFlag = showLinkTitlesFlag;
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
        BotChannel that = (BotChannel) o;
        return getId() != null && Objects.equals(getId(), that.getId())
                && getName() != null && Objects.equals(getName(), that.getName());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "BotChannel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", autoJoinFlag=" + autoJoinFlag +
                ", shortenLinksFlag=" + shortenLinksFlag +
                ", showLinkTitlesFlag=" + showLinkTitlesFlag +
                '}';
    }
}
