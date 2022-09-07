create table bot_user
(
    id        integer not null
        constraint bot_user_pk
            primary key autoincrement,
    name      text    not null
        constraint bot_user_pk
            unique,
    bot_user_hostmasks text default null,
    bot_user_flags     text default null,
    location           text default null
);
