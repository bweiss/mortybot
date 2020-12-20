package net.hatemachine.mortybot.bitly;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class References {

    @SerializedName("group")
    @Expose
    private String group;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}