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
