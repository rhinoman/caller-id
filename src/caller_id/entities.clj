(ns caller-id.entities
  (:require [schema.core :as s]
            [caller-id.phone :as phone]
            [cats.monad.maybe :as maybe]
            [taoensso.timbre :as timbre]))

;; PhoneRecord schema
(s/defschema PhoneRecord
  {:number  s/Str
   :context s/Str
   :name    s/Str})

(def p-validator (s/validator PhoneRecord))

(defn validate-phone-record [record]
  "Returns a validation result in a maybe monad"
  (try (p-validator record)
       (maybe/just record)
       (catch Exception e
         (timbre/error (.getMessage e))
         (maybe/nothing))))