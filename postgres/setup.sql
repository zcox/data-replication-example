CREATE USER example WITH PASSWORD 'example';

CREATE TABLE users (
  id bigserial primary key,
  username varchar(50) unique not null,
  name text,
  description text,
  image_url text,
  created_at timestamp not null,
  updated_at timestamp not null
);

CREATE TABLE tweets (
  id bigserial primary key,
  content text,
  latitude double precision,
  longitude double precision,
  user_id bigint not null,
  created_at timestamp not null,
  updated_at timestamp not null
);

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO example;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public to example;
