;; This ns contains the code for retrieving the data and then processing it.

(ns mashup.core
  (:use [clj-time.core :only [date-time]]
        [midje.sweet :only [facts fact]]
        [mashup.services :only [add-service exec-services]]
        [mashup.utils.date :only [add-dates date-sorter]]
        [mashup.utils.collection :only [group-by-key]])
  (:require [mashup.config :as c]
            [mashup.github :as gt]
            [mashup.twitter :as tw]))

;; This function is called exactly once the first time data is retrieved.

(defn pre-process
  "Performs the one time processing of the data after it is fetched."
  [coll]
  (-> coll
      add-dates)) ;; adds dates strings for each item.

(fact "adds dates to each item of the collection"
      (pre-process [{:a 2 :time (date-time 2011)}]) => (list {:time (date-time 2011) :a 2,
                                                              :year "2011",
                                                              :month "01-2011",
                                                              :day "01-01-2011"}))

;; This function is called each time a new request comes from the client. Data is grouped and sorted using the date key.

(defn post-process
  "The manipulataion of the data based on the dt-type, everytime it is requested."
  [dt-type coll]
  (-> coll
      (group-by-key dt-type) ;; grouping the items using the appropriate date string.
      date-sorter)) ;; sorting the items based on the date string it was

(fact "Group and sort based on the date key"
      (post-process :year [{:year "2012"} {:year "2010"} {:year "2011"}]) => {"2012" [{:year "2012"}],
                                                                              "2011" [{:year "2011"}],
                                                                              "2010" [{:year "2010"}]})

;;#### Populating the service atom
;;
;; The below calls add the twitter and github config and functions to the services atom. The services atom is then processed by the exec-services to retrieve data.
(add-service tw/srv)
(add-service gt/srv)

;; #### Fetch the data
;; The data is fetched and stored in an atom. This is to prevent the data being retrieved from the external API's multiple times.

(def retrieved-data (atom nil))

(defn fetch-it!
  "Retrieves the data if true is provided as param or if the value of the atom is nil."
  ([]
     (fetch-it! false))
  ([force]
     (if (or force (nil? @retrieved-data))
       (->>
        (exec-services)
        pre-process
        (reset! retrieved-data))
       @retrieved-data)))
