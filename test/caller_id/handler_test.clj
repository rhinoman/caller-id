(ns caller-id.handler-test
  (:require [cheshire.core :as cheshire]
            [clojure.test :refer :all]
            [caller-id.handler :refer :all]
            [caller-id.ingest-data :refer [ingest-batch]]
            [ring.mock.request :as mock]
            [caller-id.db :as db]
            [mount.core :as mount]))

(defn parse-body [body]
  (cheshire/parse-string (slurp body) true))

(def test-record
  {:number "(719) 467-8901" :context "work" :name "Alice"})

(def test-seed
  [{:number "(303) 456-7890" :context "home" :name "Steve"}
   {:number "(719) 467-8900" :context "work" :name "Sally"}
   {:number "1-201-456-7890" :context "mobile" :name "Steve"}
   {:number "(303) 456-7890" :context "vacation" :name "Steve"}])

(defn setup []
  "Setup test env"
  (mount/start)
  (db/init-db)
  (ingest-batch test-seed))

(defn cleanup []
  "Tears down test env"
  (mount/stop))

(defn api-test-fixture [f]
  (setup)
  (f)
  (cleanup))

(use-fixtures :once api-test-fixture)

(defn mock-post [record]
  (-> (mock/request :post "/api/number" (cheshire/encode record))
      (mock/content-type "application/json")))

(deftest api-test
  (testing "Test GET request that should return a single record"
    (let [response (app (-> (mock/request :get  "/api/query?number=+17194678900")))
          body     (parse-body (:body response))
          record   (first (:results body))]
      (is (= (:status response) 200))
      (is (= (:name record) "Sally"))))
  (testing "Test GET request that should return two records"
    (let [response (app (-> (mock/request :get "/api/query?number=+13034567890")))
          body     (parse-body (:body response))
          results  (:results body)]
      (is (= (:status response) 200))
      (is (= (count results) 2))
      (is (= (:name (first results)) (:name (second results)) "Steve"))))
  (testing "Test GET request that should return no results"
    (let [response (app (-> (mock/request :get "/api/query?number=+18001234567")))
          body     (parse-body (:body response))
          results  (:results body)]
      (is (= (:status response) 200))
      (is (= (count results) 0))))
  (testing "Test GET request with an invalid number"
    (let [response (app (-> (mock/request :get "/api/query?number=42")))]
      ;; Should return 400
      (is (= (:status response) 400))))
  (testing "Test GET request with a bad query parameter"
    (let [response (app (-> (mock/request :get "/api/query?lizard=iguana")))]
      ;; Should return 400
      (is (= (:status response) 400))))
  (testing "Test POST request"
    (let [response (app (-> (mock-post test-record)))
          fetch-resp (deref (db/fetch-records-by-number "+17194678901"))]
      ;; Should return 201
      (is (= (:status response) 201))
      ;; Verify it was in fact ingested
      (is (= (:name (first fetch-resp)) "Alice"))))
  (testing "Test POST request with invalid record"
    (let [response (app (-> (mock-post {:lizard "iguana"})))]
      ;; Should return 400
      (is (= (:status response) 400))))
  (testing "Test POST request with invalid number"
    (let [response (app (-> (mock-post {:number "42" :context "beach" :name "Toni"})))]
      ;; Should return 400
      (is (= (:status response) 400))))
  (testing "Test POST with no body"
    (let [response (app (-> (mock-post nil)))]
      ;; Should return 400
      (is (= (:status response) 400))))
  (testing "Test POST an already existing record"
    (let [response (app (-> (mock-post test-record)))]
      ;; Should return 409 - conflict
      (is (= (:status response) 409)))))
