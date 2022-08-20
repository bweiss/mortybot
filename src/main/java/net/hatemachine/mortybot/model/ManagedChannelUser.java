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

import javax.annotation.Generated;

public class ManagedChannelUser {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.247859-04:00", comments="Source field: managed_channel_users.managed_channel_user_id")
    private Integer managedChannelUserId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.247929-04:00", comments="Source field: managed_channel_users.managed_channel_id")
    private Integer managedChannelId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.247982-04:00", comments="Source field: managed_channel_users.bot_user_id")
    private Integer botUserId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.248052-04:00", comments="Source field: managed_channel_users.auto_op_flag")
    private Integer autoOpFlag;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.248097-04:00", comments="Source field: managed_channel_users.auto_voice_flag")
    private Integer autoVoiceFlag;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.247885-04:00", comments="Source field: managed_channel_users.managed_channel_user_id")
    public Integer getManagedChannelUserId() {
        return managedChannelUserId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.24791-04:00", comments="Source field: managed_channel_users.managed_channel_user_id")
    public void setManagedChannelUserId(Integer managedChannelUserId) {
        this.managedChannelUserId = managedChannelUserId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.247946-04:00", comments="Source field: managed_channel_users.managed_channel_id")
    public Integer getManagedChannelId() {
        return managedChannelId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.247963-04:00", comments="Source field: managed_channel_users.managed_channel_id")
    public void setManagedChannelId(Integer managedChannelId) {
        this.managedChannelId = managedChannelId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.24802-04:00", comments="Source field: managed_channel_users.bot_user_id")
    public Integer getBotUserId() {
        return botUserId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.248035-04:00", comments="Source field: managed_channel_users.bot_user_id")
    public void setBotUserId(Integer botUserId) {
        this.botUserId = botUserId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.248067-04:00", comments="Source field: managed_channel_users.auto_op_flag")
    public Integer getAutoOpFlag() {
        return autoOpFlag;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.248083-04:00", comments="Source field: managed_channel_users.auto_op_flag")
    public void setAutoOpFlag(Integer autoOpFlag) {
        this.autoOpFlag = autoOpFlag;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.248112-04:00", comments="Source field: managed_channel_users.auto_voice_flag")
    public Integer getAutoVoiceFlag() {
        return autoVoiceFlag;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.248127-04:00", comments="Source field: managed_channel_users.auto_voice_flag")
    public void setAutoVoiceFlag(Integer autoVoiceFlag) {
        this.autoVoiceFlag = autoVoiceFlag;
    }
}