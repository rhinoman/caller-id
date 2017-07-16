-- :name create-caller-table
-- :command :execute
-- :result :raw
-- :doc Create Caller table
CREATE TABLE callers (
  id       INTEGER AUTO_INCREMENT PRIMARY KEY,
  number   VARCHAR(15),
  context  VARCHAR(64),
  name     VARCHAR(64),
);

-- :name create-phone-index
-- :command :execute
-- :result :raw
-- :doc Create index on phone_number column
CREATE UNIQUE INDEX uq_phone_ctx_idx ON callers(number, context);

-- :name caller-by-row :? :1
-- :doc Get Caller by ROW ID
SELECT number, context, name FROM callers WHERE id = :id;

-- :name callers-by-phone :? :*
-- :doc Get a list of callers by phone number
SELECT number, context, name FROM callers WHERE number = :number;

-- :name insert-caller :! :n
-- :doc Inserts a single caller
INSERT INTO callers (number, context, name) VALUES (:number, :context, :name);

-- :name insert-callers :! :n
-- :doc Inserts multiple callers
INSERT INTO callers (number, context, name) VALUES :tuple*:callers;

-- :name count-callers :? :1
-- :doc returns the number of callers in the table
SELECT COUNT(*) FROM callers;