package net.hatemachine.mortybot.migration;

import java.math.BigDecimal;
import org.apache.ibatis.migration.MigrationScript;

public class V001_CreateChangelog implements MigrationScript {
    public BigDecimal getId() {
        return BigDecimal.valueOf(1L);
    }

    public String getDescription() {
        return "Create changelog table";
    }

    public String getUpScript() {
        return """
                create table changelog
                (
                    id          integer not null
                        constraint changelog_pk
                            primary key,
                    applied_at  text    not null,
                    description text    not null
                );
                                
                create unique index changelog_id_uindex
                    on changelog (id);
                                
                """;
    }

    public String getDownScript() {
        return "drop table changelog;";
    }
}