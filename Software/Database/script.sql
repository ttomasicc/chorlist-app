DROP DATABASE IF EXISTS chorlist;

CREATE DATABASE chorlist;
GRANT ALL PRIVILEGES ON DATABASE chorlist TO postgres;
\c chorlist
SET client_encoding = 'UTF8';

-- -----------------------------------------------------
-- Table user
-- -----------------------------------------------------

DROP TABLE IF EXISTS user_account CASCADE;

CREATE TABLE IF NOT EXISTS user_account (
  id        SERIAL NOT NULL,
  firstname VARCHAR(70) NOT NULL,
  lastname  VARCHAR(70) NOT NULL,
  username  VARCHAR(50) NOT NULL UNIQUE,
  email     VARCHAR(70) NOT NULL UNIQUE,
  password  CHAR(64) NOT NULL,
  PRIMARY KEY (id)
);

-- -----------------------------------------------------
-- Table shopping_list
-- -----------------------------------------------------

DROP TABLE IF EXISTS shopping_list CASCADE;

CREATE TABLE IF NOT EXISTS shopping_list (
  id            SERIAL NOT NULL,
  id_user       BIGINT NOT NULL,
  description   VARCHAR(70) NOT NULL,
  modified      DATE NOT NULL DEFAULT CURRENT_DATE,
  color         CHAR(7) NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (id_user)
    REFERENCES user_account (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

-- -----------------------------------------------------
-- Table item
-- -----------------------------------------------------

DROP TABLE IF EXISTS item CASCADE;

CREATE TABLE IF NOT EXISTS item (
  id                SERIAL NOT NULL,
  id_shopping_list  BIGINT NOT NULL,
  description       VARCHAR(70) NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (id_shopping_list)
    REFERENCES shopping_list (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

-- -----------------------------------------------------
-- FUNCTIONS
-- -----------------------------------------------------

CREATE OR REPLACE FUNCTION UPDATE_SHOPPING_LIST_LAST_MODIFIED()
  RETURNS TRIGGER AS
$$
BEGIN
  UPDATE shopping_list SET modified = CURRENT_DATE WHERE id = COALESCE(OLD.id_shopping_list, NEW.id_shopping_list);
  RETURN NULL;
END
$$ LANGUAGE PLPGSQL;

-- -----------------------------------------------------
-- TRIGGERS
-- -----------------------------------------------------

CREATE OR REPLACE TRIGGER item_changed
  AFTER INSERT OR UPDATE OR DELETE
  ON item
  FOR EACH ROW
  EXECUTE PROCEDURE UPDATE_SHOPPING_LIST_LAST_MODIFIED();

\dt
\df