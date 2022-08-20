create table managed_channel_users
(
    managed_channel_user_id integer not null
        constraint managed_channel_users_managed_channel_user_id_pk
            primary key autoincrement,
    managed_channel_id      integer not null
        constraint managed_channel_users_managed_channel_user_id_fk
            references managed_channels (managed_channel_id),
    bot_user_id             integer not null
        constraint managed_channel_users_bot_user_id_fk
            references bot_users (bot_user_id),
    auto_op_flag            integer,
    auto_voice_flag         integer
);

create index managed_channel_users_bot_user_id_index
    on managed_channel_users (bot_user_id);

create index managed_channel_users_managed_channel_id_index
    on managed_channel_users (managed_channel_id);

create unique index managed_channel_users_managed_channel_user_id_uindex
    on managed_channel_users (managed_channel_user_id);

