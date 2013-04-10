;; The code in this ns retrieves the data in parallel for both the
;; services - github and twitter.
;; The data is then processed to include date for each item in three
;; formats - day, month and year.

(ns mashup.core
  (:use [clj-time.core :only [date-time]]
        [midje.sweet :only [facts fact]]
        [mashup.services :only [add-service exec-services]]
        [mashup.utils.date :only [add-dates date-sorter]]
        [mashup.utils.collection :only [group-by-key]])
  (:require [mashup.config :as c]
            [mashup.github :as gt]
            [mashup.twitter :as tw]))

(defn pre-process
  "The one time processing of the data after it is fetched."
  [coll]
  (-> coll
      add-dates))

(defn post-process
  "The manipulataion of the data based on the dt-type, everytime it is requested."
  [dt-type coll]
  (-> coll
      (group-by-key dt-type)
      date-sorter))

(post-process :year [{:year "2012"} {:year "2010"} {:year "2011"}])

;; Populating the service atom with the twitter and github services.

(add-service tw/srv)
(add-service gt/srv)

;; #### Fetch the data
;; The data is fetched and stored in an atom. This is to prevent the data being retrieved from the external API's multiple times.

(def retrieved-data (atom nil))

;; Retrieves the data if true is provided as param or if the value of the atom is nil.

(defn fetch-it!
  ([]
     (fetch-it! false))
  ([force]
     (if (or force (nil? @retrieved-data))
       (->>
        (exec-services)
        pre-process
        (reset! retrieved-data))
       @retrieved-data)))

;; (facts "The fetch-it! function sucessfully retrieves data"
;;        (data ))
