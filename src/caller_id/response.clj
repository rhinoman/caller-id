(ns caller-id.response
  (:require [ring.util.http-response :refer :all]
            [cats.monad.exception :as exc]
            [cats.core :as m]
            [taoensso.timbre :as timbre]
            [cats.core :refer [extract fmap]]
            [cats.monad.either :as either]))

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

