create table managed_channel
(
    id    integer not null
        constraint managed_channel_pk
            primary key autoincrement,
    name  text    not null
        constraint managed_channel_pk
            unique,
    managed_channel_flags text default null
);
