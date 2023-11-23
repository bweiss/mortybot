create table BotChannel
(
    id                 integer      not null    primary key,
    name               text         not null,
    autoJoinFlag       boolean      not null,
    shortenLinksFlag   boolean      not null,
    showLinkTitlesFlag boolean      not null
);

create unique index idx_BotChannel_name on BotChannel (name);

create table BotChannel_SEQ
(
    next_val integer
);

insert into BotChannel_SEQ (next_val) values (1);
