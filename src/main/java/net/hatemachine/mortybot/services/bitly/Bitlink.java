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
package net.hatemachine.mortybot.services.bitly;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Bitlink {

    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("link")
    @Expose
    private String link;
    @SerializedName("custom_bitlinks")
    @Expose
    private List<Object> customBitlinks = null;
    @SerializedName("long_url")
    @Expose
    private String longUrl;
    @SerializedName("archived")
    @Expose
    private Boolean archived;
    @SerializedName("tags")
    @Expose
    private List<Object> tags = null;
    @SerializedName("deeplinks")
    @Expose
    private List<Object> deeplinks = null;
    @SerializedName("references")
    @Expose
    private net.hatemachine.mortybot.services.bitly.References references;

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public List<Object> getCustomBitlinks() {
        return customBitlinks;
    }

    public void setCustomBitlinks(List<Object> customBitlinks) {
        this.customBitlinks = customBitlinks;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }

    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }

    public List<Object> getTags() {
        return tags;
    }

    public void setTags(List<Object> tags) {
        this.tags = tags;
    }

    public List<Object> getDeeplinks() {
        return deeplinks;
    }

    public void setDeeplinks(List<Object> deeplinks) {
        this.deeplinks = deeplinks;
    }

    public net.hatemachine.mortybot.services.bitly.References getReferences() {
        return references;
    }

    public void setReferences(net.hatemachine.mortybot.services.bitly.References references) {
        this.references = references;
    }
}