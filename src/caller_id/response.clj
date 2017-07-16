(ns caller-id.response
  (:require [ring.util.http-response :refer :all]
            [cats.monad.exception :as exc]
            [cats.core :as m]
            [taoensso.timbre :as timbre]
            [cats.core :refer [extract fmap]]
            [cats.monad.either :as either]))

(defn return-entity!
  "Returns Entity (or 404) from database query"
  [status-func entity]
  (if (nil? entity)
    (do
      (timbre/warn "Resource not found")
      (not-found {:reason "Resource not found"}))
    (status-func entity)))

(defn handle-internal-error
  "Logs and sends 500 response"
  [err]
  (timbre/error "ERROR occured! " (.getName (.getClass err)) (.getMessage err))
  (internal-server-error {:reason "Internal Server Error"}))

(defn get-response
  "Process a Get request. Expects an either monad"
  [result-m]
  (either/branch result-m #(apply % nil) (fn [entity]
                                           (if (nil? entity)
                                             (not-found {:reason "Resource not found"})
                                             (ok {:results entity})))))
(defn post-response
  "Process a POST request -- expects an either monad"
  ([result-m] (either/branch result-m #(apply % nil) (fn [x] (created)))))

