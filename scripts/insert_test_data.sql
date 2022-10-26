INSERT INTO bot_user (name, bot_user_hostmasks, bot_user_flags, location) VALUES ('test1', 'test1!test@fake.domain.localhost', null, null);
INSERT INTO bot_user (name, bot_user_hostmasks, bot_user_flags, location) VALUES ('test2', 'test2!test@fake.domain.localhost', null, null);
INSERT INTO bot_user (name, bot_user_hostmasks, bot_user_flags, location) VALUES ('test3', 'test3!test@fake.domain.localhost', null, null);
INSERT INTO bot_user (name, bot_user_hostmasks, bot_user_flags, location) VALUES ('test4', 'test4!test@fake.domain.localhost', null, null);
INSERT INTO bot_user (name, bot_user_hostmasks, bot_user_flags, location) VALUES ('test5', 'test5!test@fake.domain.localhost', null, null);
INSERT INTO bot_user (name, bot_user_hostmasks, bot_user_flags, location) VALUES ('test6', 'test6!test@fake.domain.localhost', null, null);
INSERT INTO bot_user (name, bot_user_hostmasks, bot_user_flags, location) VALUES ('test7', 'test7!test@fake.domain.localhost', null, null);
INSERT INTO bot_user (name, bot_user_hostmasks, bot_user_flags, location) VALUES ('test8', 'test8!test@fake.domain.localhost', null, null);
INSERT INTO bot_user (name, bot_user_hostmasks, bot_user_flags, location) VALUES ('test9', 'test9!test@fake.domain.localhost', null, null);
INSERT INTO bot_user (name, bot_user_hostmasks, bot_user_flags, location) VALUES ('test10', 'test10!test@fake.domain.localhost', null, null);
INSERT INTO bot_user (name, bot_user_hostmasks, bot_user_flags, location) VALUES ('test11', 'test11!test@fake.domain.localhost', null, null);
INSERT INTO bot_user (name, bot_user_hostmasks, bot_user_flags, location) VALUES ('test12', 'test12!test@fake.domain.localhost', null, null);
INSERT INTO bot_user (name, bot_user_hostmasks, bot_user_flags, location) VALUES ('test13', 'test13!test@fake.domain.localhost', null, null);
INSERT INTO bot_user (name, bot_user_hostmasks, bot_user_flags, location) VALUES ('test14', 'test14!test@fake.domain.localhost', null, null);
INSERT INTO bot_user (name, bot_user_hostmasks, bot_user_flags, location) VALUES ('test15', 'test15!test@fake.domain.localhost', null, null);
INSERT INTO bot_user (name, bot_user_hostmasks, bot_user_flags, location) VALUES ('test16', 'test16!test@fake.domain.localhost', null, null);
INSERT INTO bot_user (name, bot_user_hostmasks, bot_user_flags, location) VALUES ('test17', 'test17!test@fake.domain.localhost', null, null);
INSERT INTO bot_user (name, bot_user_hostmasks, bot_user_flags, location) VALUES ('test18', 'test18!test@fake.domain.localhost', null, null);
INSERT INTO bot_user (name, bot_user_hostmasks, bot_user_flags, location) VALUES ('test19', 'test19!test@fake.domain.localhost', null, null);
INSERT INTO bot_user (name, bot_user_hostmasks, bot_user_flags, location) VALUES ('test20', 'test20!test@fake.domain.localhost', null, null);
INSERT INTO bot_user (name, bot_user_hostmasks, bot_user_flags, location) VALUES ('test21', 'test21!test@fake.domain.localhost', null, null);
INSERT INTO bot_user (name, bot_user_hostmasks, bot_user_flags, location) VALUES ('test22', 'test22!test@fake.domain.localhost', null, null);

INSERT INTO managed_channel (name, managed_channel_flags, bans, modes) VALUES ('#mortybot', '["AUTO_JOIN","SHORTEN_LINKS"]', 'foo!bar@baz,foo!qux@quux', '+nt');
INSERT INTO managed_channel (name, managed_channel_flags, bans, modes) VALUES ('#test', '["SHORTEN_LINKS"]', null, null);

INSERT INTO managed_channel_user (managed_channel_id, bot_user_id, managed_channel_user_flags) VALUES (1, 1, '["AUTO_OP","AUTO_VOICE"]');
INSERT INTO managed_channel_user (managed_channel_id, bot_user_id, managed_channel_user_flags) VALUES (2, 1, '["AUTO_OP"]');
