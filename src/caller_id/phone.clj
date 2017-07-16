(ns caller-id.phone
  (:require [cats.monad.either :as either]
            [taoensso.timbre :as timbre]
            [cats.core :as m]
            [cats.monad.maybe :as maybe])
  (:import (com.google.i18n.phonenumbers PhoneNumberUtil
                                         NumberParseException
                                         PhoneNumberUtil$PhoneNumberFormat
                                         Phonenumber$PhoneNumber MetadataManager)))

;; phone-util object for usage
(def phone-util (PhoneNumberUtil/getInstance))

(defn valid-number? [^Phonenumber$PhoneNumber num]
  "does a length check to determine validity"
  (.isPossibleNumber phone-util num))


(defn parse-number [^String num]
  "Parses a phone number string into a PhoneNumber object.
   Returns an either monad"
  (try (let [phone (.parse phone-util num "US")]
         (if (valid-number? phone)
           (either/right phone)
           (either/left "Not a valid phone number")))
       (catch NumberParseException npe
         (timbre/error (str "Exception caught: " (.getMessage npe)))
         (either/left (.getMessage npe)))))

(defn format-number ^String [^Phonenumber$PhoneNumber num]
  "Formats the phone number as E164"
  (.format phone-util num PhoneNumberUtil$PhoneNumberFormat/E164))

(def format-number-m (m/lift-m format-number))

(defn e164-from-string [^String num]
  "Takes in a string and returns an e164 formatted string wrapped in an either monad"
  (-> num
      (parse-number)
      (format-number-m)))


(defn e164-from-record-m [record-m]
  "Takes a record wrapped in a maybe monad and formats its number string.
   Returning another maybe monad"
  (let [record (maybe/from-maybe record-m)
        num (e164-from-string (:number record))]
    (either/branch num
                   (fn [e]
                     (timbre/error "Error on number: " (:number record) e)
                     (maybe/nothing))
                   #(maybe/just (assoc record :number %)))))
