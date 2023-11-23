create table BotUser
(
    id         integer      not null    primary key,
    name       text         not null,
    password   text,
    location   text,
    adminFlag  boolean      not null,
    dccFlag    boolean      not null,
    ignoreFlag boolean      not null
);

create unique index idx_BotUser_name on BotUser (name);

create table BotUser_SEQ
(
    next_val integer
);

insert into BotUser_SEQ (next_val) values (1);

create table BotUser_autoOpChannels
(
    BotUser_id     integer not null,
    autoOpChannels text
);

create table BotUser_hostmasks
(
    BotUser_id integer not null,
    hostmasks  text
);
