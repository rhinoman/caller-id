(ns caller-id.ingest-test
  (:require [clojure.test :refer :all]
            [caller-id.ingest-data :refer :all]
            [cats.monad.either :as either]
            [mount.core :as mount]
            [caller-id.db :as db]
            [taoensso.timbre :as timbre]))

;; Setup a test fixture
;; Need to start/top the database connection for this one
(defn db-test-fixture [f]
  (mount/start)
  (db/init-db)
  (f)
  (mount/stop))

(use-fixtures :once db-test-fixture)

(def test-record
  {:number "(719) 467-8901" :context "work" :name "Alice"})

(def test-batch
  [{:number "(303) 456-7890" :context "home" :name "Steve"}
   {:number "(719) 467-8900" :context "work" :name "Sally"}
   {:number "1-201-456-7890" :context "mobile" :name "Steve"}])

(deftest test-ingest
  (testing "Test ingest-record function"
    (let [result (ingest-record test-record)
          num (db/num-callers)
          fetched (db/fetch-records-by-number "+17194678901")]
      (is (either/right? result))
      (is (= num 1))
      (is (either/right? fetched))
      (is (= (:name (first (deref fetched))) "Alice")))))

(deftest test-ingest-batch
  (testing "Test ingest-batch function"
    (let [result (ingest-batch test-batch)
          num (db/num-callers)
          fetch1 (db/fetch-records-by-number "+13034567890")]
      (is (either/right? fetch1))
      (is (>= num 3))
      (is (= (:name (first (deref fetch1))) "Steve")))))

(deftest test-ingest-seed-data
  (testing "Test ingest the seed data"
    (ingest-seed-data)
    (let [num (db/num-callers)]
      (is (>= num 499996))
      (timbre/info "Number of rows: " num))))