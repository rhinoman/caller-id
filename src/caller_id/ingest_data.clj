(ns caller-id.ingest-data
  (:require [caller-id.db :as db]
            [clojure.java.io :as io]
            [clojure.data.csv :as csv]
            [semantic-csv.core :as sc :refer :all]
            [caller-id.entities :refer [PhoneRecord
                                        validate-phone-record]]
            [schema.core :as s]
            [cats.monad.either :as either]
            [taoensso.timbre :as timbre]
            [cats.monad.maybe :as maybe]
            [caller-id.phone :as phone]
            [ring.util.http-response :as resp]))

(def seed-data-file "data")
(def ingest-batch-size 50000)

(defn prep-record [record]
  "Validates a record, logs error if any.  Returns a Maybe Monad"
  (-> record
      (validate-phone-record)
      (phone/e164-from-record-m)))

(defn ingest-record [record]
  "Ingest a single record.  Returns an Either monad"
  (maybe/maybe (either/left #(resp/bad-request {:reason "Bad Input"}))
               (prep-record record)
               #(db/insert-record! %)))

(defn ingest-batch [batch]
  "processes a batch of records"
  (timbre/info "ingesting...")
  (->> batch
       (map prep-record)
       (maybe/cat-maybes)
       (doall)
       (db/insert-records!)))

(defn ingest-seed-data []
  "Ingests the seed data"
  (with-open [data (io/reader (io/resource seed-data-file))]
    (let [batches (->> (csv/read-csv data)
                       (mappify {:header ["number" "context" "name"]})
                       (partition-all ingest-batch-size))]
      (doseq [b batches] (ingest-batch b)))))
