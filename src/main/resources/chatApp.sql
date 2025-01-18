-- CREATE TABLE tbl_user
-- (
--     id        BIGSERIAL PRIMARY KEY,
--     username  VARCHAR,
--     password  VARCHAR,
--     full_name VARCHAR,
--     image_url VARCHAR,
--     background_url VARCHAR,
--     birthday  TIMESTAMP,
--     gender    VARCHAR,
--     description VARCHAR
-- );

-- CREATE TABLE tbl_group
-- (
-- 	id BIGSERIAL primary key,
-- 	name varchar,
-- 	member_count Integer,
-- 	image_url varchar
-- );

-- CREATE TABLE tbl_user_group
-- (
-- 	id BIGSERIAL primary key,
-- 	user_id BIGSERIAL REFERENCES tbl_user(id),
-- 	group_id BIGSERIAL REFERENCES tbl_group(id),
-- 	role varchar
-- );

-- CREATE TABLE tbl_post
-- (
-- 	id BIGSERIAL primary key,
-- 	user_id BIGSERIAL REFERENCES tbl_user(id),
-- 	content varchar,
-- 	image_urls_string varchar,
-- 	create_at TIMESTAMP,
-- 	type varchar,
-- 	state varchar
-- );

-- CREATE TABLE tbl_post_group
-- (
-- 	id BIGSERIAL primary key,
-- 	group_id BIGSERIAL REFERENCES tbl_group(id),
-- 	post_id BIGSERIAL REFERENCES tbl_post(id)
-- );

-- CREATE TABLE tbl_comment
-- (
-- 	id BIGSERIAL primary key,
-- 	user_id BIGSERIAL REFERENCES tbl_user(id),
-- 	post_id BIGSERIAL REFERENCES tbl_post(id),
-- 	comment varchar,
-- 	image_url varchar,
-- 	create_at TIMESTAMP
-- );

-- CREATE TABLE tbl_like_post
-- (
-- 	id BIGSERIAL primary key,
-- 	user_id BIGSERIAL REFERENCES tbl_user(id),
-- 	post_id BIGSERIAL REFERENCES tbl_post(id)
-- );

-- CREATE TABLE tbl_share_post
-- (
-- 	id BIGSERIAL primary key,
-- 	user_id BIGSERIAL REFERENCES tbl_user(id),
-- 	post_id BIGSERIAL REFERENCES tbl_post(id)
-- );

-- CREATE TABLE tbl_friend_request
-- (
--     id          BIGSERIAL PRIMARY KEY,
--     sender_id   BIGINT REFERENCES tbl_user (id),
--     receiver_id BIGINT REFERENCES tbl_user (id),
--     created_at  TIMESTAMP
-- );

-- CREATE TABLE tbl_friend_map
-- (
--     id        BIGSERIAL PRIMARY KEY,
--     user_id_1 BIGINT REFERENCES tbl_user (id),
--     user_id_2 BIGINT REFERENCES tbl_user (id)
-- );

--ALTER TABLE tbl_post ADD COLUMN share_id bigserial;
--ALTER TABLE tbl_post DROP COLUMN create_at;
--ALTER TABLE tbl_post ADD COLUMN group_id bigserial;
--ALTER TABLE tbl_comment ADD COLUMN comment_id bigserial;

-- create table tbl_chat
-- (
--     id Bigserial Primary Key,
--     name varchar,
--     chat_type varchar,
--     managerId BIGINT references tbl_user(id),
--     user_id1 BIGINT references tbl_user(id),
--     user_id2 BIGINT references tbl_user(id),
--     newest_user_id BIGINT references tbl_user(id),
--     newest_message varchar,
--     image_url varchar
-- );

-- create table tbl_user_chat_map (
--     id BigSerial Primary Key,
--     user_id BIGINT references tbl_user(id),
--     chat_id BIGINT references tbl_chat(id)
-- )

-- create table tbl_message
-- (
--     id Bigserial Primary Key,
--     sender_id BIGINT references tbl_user(id),
--     chat_id1 BIGINT references tbl_chat(id),
--     chat_id2 BIGINT references tbl_chat(id),
--     group_chat_id BIGINT references tbl_chat(id),
--     message varchar,
--     create_at Timestamp
-- )

-- create table tbl_notification
-- (
--     id Bigserial Primary Key,
--     user_id BIGINT References tbl_user(id),
--     interact_id BIGINT References tbl_user(id),
--     group_id BIGINT References tbl_group(id),
--     post_id BIGINT References tbl_post(id),
--     create_at Timestamp,
--     has_seen boolean,
--     type varchar,
--     interact_type varchar
-- )

-- create table tbl_event_notification
-- (
--     id Bigserial Primary Key,
--     user_id BIGINT references tbl_user(id),
--     event_type varchar,
--     state varchar,
--     chat_id BIGINT references tbl_chat(id),
-- )

--ALTER TABLE tbl_post ALTER COLUMN share_id DROP NOT NULL;
--ALTER TABLE tbl_post ALTER COLUMN group_id DROP NOT NULL;