create table managed_channels
(
    managed_channel_id INTEGER not null
        constraint managed_channels_pk
            primary key autoincrement,
    name               TEXT    not null,
    auto_join_flag     INTEGER,
    modes              TEXT,
    enforce_modes_flag INTEGER
);

create unique index managed_channels_managed_channel_id_uindex
    on managed_channels (managed_channel_id);

create unique index managed_channels_name_uindex
    on managed_channels (name);

