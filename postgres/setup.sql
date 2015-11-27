CREATE USER example WITH PASSWORD 'example';

create extension bottledwater;

CREATE TABLE users (
  id bigserial primary key,
  username varchar(50) unique not null,
  name text,
  description text,
  image_url text
);

CREATE TABLE tweets (
  id bigserial primary key,
  content text,
  created_at timestamp,
  latitude double precision,
  longitude double precision,
  user_id integer not null
);

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO example;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public to example;
