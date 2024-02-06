---- BotChannel
create table BotChannel_dg_tmp
(
    id                 integer             not null     primary key,
    name               text collate NOCASE not null,
    autoJoinFlag       boolean             not null,
    shortenLinksFlag   boolean             not null,
    showLinkTitlesFlag boolean             not null
);

insert into BotChannel_dg_tmp(id, name, autoJoinFlag, shortenLinksFlag, showLinkTitlesFlag)
select id, name, autoJoinFlag, shortenLinksFlag, showLinkTitlesFlag
from BotChannel;

drop table BotChannel;

alter table BotChannel_dg_tmp
    rename to BotChannel;

create unique index idx_BotChannel_name
    on BotChannel (name);

---- BotUser
create table BotUser_dg_tmp
(
    id         integer             not null     primary key,
    name       text collate NOCASE not null,
    password   text,
    location   text,
    adminFlag  boolean             not null,
    dccFlag    boolean             not null,
    ignoreFlag boolean             not null
);

insert into BotUser_dg_tmp(id, name, password, location, adminFlag, dccFlag, ignoreFlag)
select id, name, password, location, adminFlag, dccFlag, ignoreFlag
from BotUser;

drop table BotUser;

alter table BotUser_dg_tmp
    rename to BotUser;

create unique index idx_BotUser_name
    on BotUser (name);

---- BotUser_autoOpChannels
create table BotUser_autoOpChannels_dg_tmp
(
    BotUser_id     integer not null,
    autoOpChannels text collate NOCASE
);

insert into BotUser_autoOpChannels_dg_tmp(BotUser_id, autoOpChannels)
select BotUser_id, autoOpChannels
from BotUser_autoOpChannels;

drop table BotUser_autoOpChannels;

alter table BotUser_autoOpChannels_dg_tmp
    rename to BotUser_autoOpChannels;
