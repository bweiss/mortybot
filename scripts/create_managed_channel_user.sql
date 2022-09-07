create table managed_channel_user
(
    id                 integer not null
        constraint managed_channel_user_pk
            primary key autoincrement,
    managed_channel_id         integer not null,
    bot_user_id                integer not null,
    managed_channel_user_flags text default null
);

create index managed_channel_user_bot_user_id_index
    on managed_channel_user (bot_user_id);

create index managed_channel_user_managed_channel_id_index
    on managed_channel_user (managed_channel_id);

