CREATE TABLE IF NOT EXISTS node
(
  id        BIGINT PRIMARY KEY NOT NULL,
  lat       DOUBLE PRECISION,
  lon       DOUBLE PRECISION,
  username  VARCHAR(256),
  uid       BIGINT,
  visible   BOOLEAN,
  version   BIGINT,
  changeset BIGINT,
  time      TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tag
(
  node_id BIGINT NOT NULL,
  k       VARCHAR(255),
  v       VARCHAR(255)
);