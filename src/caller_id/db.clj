(ns caller-id.db
  (:require [conman.core :as conman]
            [hugsql.core :as hugsql]
            [mount.core :refer [defstate]]
            [cats.monad.either :as either]
            [ring.util.http-response :as resp]
            [taoensso.timbre :as timbre]
            [clojure.java.jdbc :as jdbc]
            [cats.core :as m])
  (:import (java.sql SQLException)))

;; Our database settings
(def db-settings {:jdbc-url "jdbc:h2:mem:callerid"})

(defn connect! []
  "Establishes a Database connection"
  (conman/connect! db-settings))

(defn disconnect! [conn]
  "Destroys database connection"
  (conman/disconnect! conn))

(defstate ^:dynamic *db*
          :start (connect!)
          :stop (disconnect! *db*))

;; This exposes the SQL queries in 'queries.sql' as Clojure functions
(conman/bind-connection *db* "caller_id/sql/queries.sql")

(defn init-db []
  "Creates database tables"
  ;; Creates the Caller SQL Table in H2
  (create-caller-table)
  ;; Creates a Unique index on (number, context)
  (create-phone-index))

(defn handle-sql-error
  "Logs and sets an aprropriate response"
  [se]
  (let [sqlstate (.getSQLState se)]
    (timbre/warn (str "SQL Error occured with state: " sqlstate "\n") se)
    (cond
      (= "23505" sqlstate) #(resp/conflict {:reason "Entity conflicts with existing resource"})
      (= "23514" sqlstate) #(resp/conflict {:reason "Conflict: A newer version of this data exists on the server"})
      :else #(resp/internal-server-error {:reason "Database error"}))))

(defn handle-other-error
  "Something bad, but not a sql exception, happened while accessing the database"
  [ex]
  (timbre/error (str "Error occurred! " (.getName (.getClass ex)) " " (.getMessage ex) "\n") ex)
  #(resp/internal-server-error {:reason "Error while accessing database"}))

(defn wrap-db
  "Wraps a database query.  Returns an Either monad"
  [query]
  (try (either/right (query))
       (catch SQLException se (either/left (handle-sql-error se)))
       (catch Exception ex (either/left (handle-other-error ex)))))

(defn insert-record! [record]
  "Inserts a single record into the database"
  (wrap-db #(insert-caller record)))

(defn insert-records! [records]
  "Inserts records into the database"
  (conman/with-transaction
    [*db*]
    (doseq [r records] (wrap-db #(insert-caller r)))))

(defn fetch-records-by-number [^String number]
  "Fetches an individual record given a phone number"
  (wrap-db #(callers-by-phone {:number number})))

(defn fetch-record-by-id [^Integer id]
  "Fetches an individual record given its row id"
  (wrap-db #(caller-by-row {:id id})))

(defn num-callers []
  "counts the number of callers in the database table"
  (let [c (count-callers)]
    (val (first c))))
