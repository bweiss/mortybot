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

public class ManagedChannel {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.246552-04:00", comments="Source field: managed_channels.managed_channel_id")
    private Integer managedChannelId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.246636-04:00", comments="Source field: managed_channels.name")
    private String name;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.246693-04:00", comments="Source field: managed_channels.auto_join_flag")
    private Integer autoJoinFlag;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.246749-04:00", comments="Source field: managed_channels.modes")
    private String modes;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.246808-04:00", comments="Source field: managed_channels.enforce_modes_flag")
    private Integer enforceModesFlag;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.246583-04:00", comments="Source field: managed_channels.managed_channel_id")
    public Integer getManagedChannelId() {
        return managedChannelId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.246609-04:00", comments="Source field: managed_channels.managed_channel_id")
    public void setManagedChannelId(Integer managedChannelId) {
        this.managedChannelId = managedChannelId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.246654-04:00", comments="Source field: managed_channels.name")
    public String getName() {
        return name;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.246673-04:00", comments="Source field: managed_channels.name")
    public void setName(String name) {
        this.name = name;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.246713-04:00", comments="Source field: managed_channels.auto_join_flag")
    public Integer getAutoJoinFlag() {
        return autoJoinFlag;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.246732-04:00", comments="Source field: managed_channels.auto_join_flag")
    public void setAutoJoinFlag(Integer autoJoinFlag) {
        this.autoJoinFlag = autoJoinFlag;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.246766-04:00", comments="Source field: managed_channels.modes")
    public String getModes() {
        return modes;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.246791-04:00", comments="Source field: managed_channels.modes")
    public void setModes(String modes) {
        this.modes = modes;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.246827-04:00", comments="Source field: managed_channels.enforce_modes_flag")
    public Integer getEnforceModesFlag() {
        return enforceModesFlag;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.246844-04:00", comments="Source field: managed_channels.enforce_modes_flag")
    public void setEnforceModesFlag(Integer enforceModesFlag) {
        this.enforceModesFlag = enforceModesFlag;
    }
}