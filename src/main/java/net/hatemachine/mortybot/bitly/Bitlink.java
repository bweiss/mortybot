package net.hatemachine.mortybot.bitly;

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
    private net.hatemachine.mortybot.bitly.References references;

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

    public net.hatemachine.mortybot.bitly.References getReferences() {
        return references;
    }

    public void setReferences(net.hatemachine.mortybot.bitly.References references) {
        this.references = references;
    }
}