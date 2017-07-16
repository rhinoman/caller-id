(ns caller-id.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer [bad-request!
                                             internal-server-error!]]
            [caller-id.response :as resp]
            [schema.core :as s]
            [caller-id.entities :refer [PhoneRecord]]
            [caller-id.ingest-data :refer [ingest-record]]
            [caller-id.phone :as phone]
            [caller-id.db :refer [fetch-records-by-number]]
            [caller-id.db :as db]
            [cats.monad.either :as either]
            [taoensso.timbre :as timbre]))

(defn wrap-request-logging
  "Logs incoming request"
  [handler]
  (fn [req]
    (timbre/info "REQ FROM "
                 (:remote-addr req)
                 (:request-method req)
                 (:uri req)
                 (:query-string req))
    (handler req)))

(def app
  (api
    {:swagger
     {:ui "/"
      :spec "/swagger.json"
      :data {:info {:title "Caller-Id"
                    :description "Caller ID API"}
             :tags [{:name "api", :description "API for Caller ID Data"}]}}}

    (context "/api" []
      :tags ["api"]

      ;; This returns a collection of records matching the numbers
      ;; Debated whether to return an empty collection w/ a 200 or a 404 in the case of nothing found
      ;; Decided to go with the empty collection
      ;; I find its easier/cleaner to deal with an empty list rather than an error code on the front end, anyway.
      (GET "/query" []
        :return {:results [PhoneRecord]}
        :middleware [wrap-request-logging]
        :query-params [number :- String]
        :summary "Given a phone number, returns a list of matching records"
        (let [phone-number (phone/e164-from-string number)]
          (either/branch (phone/e164-from-string number)
                         #(bad-request! {:reason %})
                         #(resp/get-response (fetch-records-by-number %)))))

      (POST "/number" []
        :middleware [wrap-request-logging]
        :body [pr PhoneRecord]
        :summary "Adds a record to the database"
        (resp/post-response (ingest-record pr))))))
