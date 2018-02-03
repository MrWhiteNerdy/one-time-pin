CREATE TABLE pins (
  oid              MEDIUMINT    NOT NULL AUTO_INCREMENT PRIMARY KEY,
  account          VARCHAR(256) NOT NULL,
  pin              VARCHAR(6)   NOT NULL,
  create_timestamp TIMESTAMP    NOT NULL,
  create_ip        VARCHAR(256) NOT NULL,
  create_user      VARCHAR(256) NOT NULL,
  expire_timestamp TIMESTAMP    NOT NULL,
  claim_timestamp  TIMESTAMP,
  claim_user       VARCHAR(256),
  claim_ip         VARCHAR(256)
);

CREATE INDEX pin ON pins (pin);
