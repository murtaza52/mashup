;; The code in this ns retrieves the data in parallel for both the
;; services - github and twitter.
;; The data is then processed to include date for each item in three
;; formats - day, month and year.

(ns mashup.core
  (:use [clj-time.core :only [date-time]]
        [midje.sweet :only [facts fact]]
        [clojure.set :only [difference]]
        [mashup.services :only [add-service exec-services]]
        [mashup.utils.date :only [floor-and-string]]
        [mashup.utils.collection :only [map-and-zip]])
  (:require [mashup.config :as c]
            [mashup.github :as gt]
            [mashup.twitter :as tw]))

;; The date types which will be added to each item.
(def dt-keys [:day :month :year])

(def make-date-strings (map-and-zip floor-and-string dt-keys))

(make-date-strings (date-time 2011 2 3 4 5))

;; Populating the service atom with the twitter and github services.

(add-service tw/srv)
(add-service gt/srv)

;; #### Fetch the data
;; The data is fetched and stored in an atom. This is to prevent the data being retrieved from the external API's multiple times.

(def retrieved-data (atom nil))

;; Retrieves the data if true is provided as param or if the value of the atom is nil.

(defn fetch-it!
  [fetch-again?]
  (if (or fetch-again?
            (nil? @retrieved-data))
    (->> (exec-services)
         (map #(-> (make-date-strings (:time %))
                   (merge %)))
         (reset! retrieved-data))
    @retrieved-data))

;; (facts "The fetch-it! function sucessfully retrieves data"
;;        (data ))
