package net.hatemachine.mortybot.migration;

import org.apache.ibatis.migration.MigrationScript;

import java.math.BigDecimal;

public class V002_CreateBotUsers implements MigrationScript {
    public BigDecimal getId() {
        return BigDecimal.valueOf(2L);
    }

    public String getDescription() {
        return "Create bot_users table";
    }

    public String getUpScript() {
        return """
                create table bot_users
                (
                    bot_user_id integer not null
                        constraint bot_users_pk
                            primary key autoincrement,
                    username    text    not null,
                    hostmasks   text,
                    flags       text,
                    location    text
                );
                                
                create unique index bot_users_bot_user_id_uindex
                    on bot_users (bot_user_id);
                                
                create unique index bot_users_username_uindex
                    on bot_users (username);
                """;
    }

    public String getDownScript() {
        return "drop table bot_users;";
    }
}
