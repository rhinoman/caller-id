(ns caller-id.core
  (:gen-class)
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [clojure.tools.cli :as cli]
            [mount.core :as mount]
            [caller-id.db :as db]
            [caller-id.ingest-data :refer [ingest-seed-data]]
            [caller-id.handler :refer [app]]
            [taoensso.timbre :as timbre]))

(timbre/set-level! :info)

(def cli-options
  ;; Set command line options
  [["-p" "--port PORT" "Port number"
    :default 8001
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]])

(defn -main [& args]
  (let [cli-opts (cli/parse-opts args cli-options)]
    (if (:errors cli-opts)
      ;; If the command line options are erroneous, output the error
      (timbre/error (reduce #(str %1 ", " %2) (:errors cli-opts)))
      ;; Otherwise, start jetty
      (let [port (get-in cli-opts [:options :port])]
        (timbre/info "Initializing Database...")
        (mount/start)
        (db/init-db)
        (timbre/info "Ingesting seed data...")
        (ingest-seed-data)
        (timbre/info "Starting Jetty on port" port)
        (run-jetty app {:port port})))))
