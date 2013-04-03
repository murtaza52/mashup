;; ### Remote API's
;; This ns exposes the API calls that can be remotely called from the
;; client. It uses the shoreleave library's defremote macro to expose
;; the remote calls. The defremote macro wraps any functions and exposes
;; them at the _shoreleave url.


(ns mashup.remote
  (:use [shoreleave.middleware.rpc :only [defremote]]
        [mashup.core :only [fetch-it!]]
        [midje.sweet :only [facts fact]]
        [clojure.set :only [subset? difference]]
        [clojure.string :only [split]]
        [clj-time.core :only [before?]]
        [clojure.set :only [map-invert]]
        [clj-time.core :only [date-time]]
        [clojure.pprint :only [pprint]])
  (:require [mashup.config :as config]))


;; #### Fetch the data
;; The data is fetched and stored in a var. This is to prevent multiple
;; calls to the server when the data is requested multiple times by the
;; client.
(def data (fetch-it!))

;; ### Utility Functions
;; Functions for grouping, sorting and removing date instances from the data.

(defn group-data
  "Groups the data based on the dt-type, ie either the day, month or the year key."
  [data dt-type]
  (group-by #(%1 dt-type)
            data))

;; Test if the data is grouped correctly.
(facts "Data is grouped by the given keys."
       (let [grouped-data (group-data data :month)
             months (map :month data)
             grouped-by-keys (keys grouped-data)]
         (fact "The grouping keys are a subset of the :month/:day keys in data"
               [months grouped-by-keys] => (fn [[s1 s2]]
                                             (subset? (set s1) (set s2))))
         (fact "All the grouping keys are distinct"
               grouped-by-keys => #(apply distinct? %))
         (fact "All the distinct keys from data are present in grouping keys"
               [months grouped-by-keys] => (fn [[s1 s2]]
                                             (empty? (difference (set s1) (set s2)))))))

;; #### Date Removal functionality

(defn dissoc-with-pred
  [f]
  (fn [m]
    (into {} (filter (complement (fn [[k v]] (f k v))) m))))

(fact "returns a map with the keys dissoc whereever the pred is true"
      (let [greater-than-2 (dissoc-with-pred (fn [k v] (> v 2)))]
        (greater-than-2 {:a 3 :b 1 :c 7 :d 2}) => {:b 1 :d 2}))

;; dissoc any keys with the type of date-time
(def dissoc-date-time
  (dissoc-with-pred (fn [_ v] (= org.joda.time.DateTime (type v)))))

(fact "dissoc any keys having value of type org.joda.time.DateTime"
      (dissoc-date-time {:time (date-time 2013) :a 2 :b "hello"}) => #(every? (fn [[k v]] (not= (type v) org.joda.time.DateTime)) %))

;; #### Sorting Functionality

(defn date-time-comparator
  "Predicate function for comparing two date-time's"
  [time1 time2]
  (before? time1 time2))

(fact "Returns true if the first date is chronologically before the second date."
      (date-time-comparator (date-time 2011) (date-time 2013)) => true)

(defn get-time-value
  "Function for extracting the date-time from the value of the given map."
  [v]
  (-> v first :time))

(fact "Given [{:a 2 :time 3} {:time 4 :a 5}] returns the :time from the first map."
      (get-time-value [{:a 2 :time 3} {:time 4 :a 5}]) => 3)

(defn sort-map-by-value
  "Given a map return a sorted map, in which the sort is done on the map's values, instead of keys.
   Takes a function as an input, which will be used  for sorting
   cf -> The comparator function
   kf -> The key function which returns the value that is the be compared
   m -> The map which has to be sorted"
  [cf kf m]
  (->> m
       map-invert
       (into (sorted-map-by #(cf (kf %1) (kf %2))))
       map-invert))

(def sort-map-by-date (partial sort-map-by-value date-time-comparator get-time-value))

(fact "Given an input as { 'ab' [ {:time (date-time ...)..} .. ] ..} sorts the map based on the time key of the first element."
      (sort-map-by-date {"3-19-2013" [{:time (date-time 2013 3 19 12 14 45)}]
                         "3-9-2013" [{:time (date-time 2013 3 9 16 46 49)}]
                         "2-25-2013" [{:time (date-time 2013 2 25 2 38 15)}]
                         "3-14-2013" [{:time (date-time 2013 3 14 7 19 23)}]
                         "2-8-2013" [{:time (date-time 2013 2 8 12 44 47)}]}) => {"3-19-2013" [{:time (date-time 2013 3 19 12 14 45)}]
                                                                                  "3-14-2013" [{:time (date-time 2013 3 14 7 19 23)}]
                                                                                  "3-9-2013" [{:time (date-time 2013 3 9 16 46 49)}]
                                                                                  "2-25-2013" [{:time (date-time 2013 2 25 2 38 15)}]
                                                                                  "2-8-2013" [{:time (date-time 2013 2 8 12 44 47)}]})

(defn remove-date
  [data]
  (->>
   (map (fn [[k v]]
         (let [new-v (mapv dissoc-date-time v)]
           {k new-v}))
        data)
   (into {})))

(fact "Removes all date-time instances from the following data structure {k [{v}]}"
      (remove-date {:a [{:time (date-time 2012) :e 2} {:f 3}]}) => (fn [data]
                                                                     (every? (fn [[k v]] not= (type v) org.joda.time.DateTime)
                                                                             (-> data vals first first))))

(defremote fetch-data
  [dt-type]
  (->
   data
   (group-data dt-type)
   sort-map-by-date
   remove-date))

(facts "Checking the returned data"
       (fact "The data is free of any instance of org.joda.time.DateTime"
             (fetch-data :month) => (fn [data]
                                      (every? (fn [[k v]] not= (type v) org.joda.time.DateTime)
                                              (-> data vals first first))))
       (fact "The returned data is grouped"
             (fetch-data :day) => #(-> % keys empty? not)))

(defremote get-twitter-uname
  []
  config/tw-screen-name)

(fact "Returns a string"
      (get-twitter-uname) => #(-> % type (= java.lang.String)))
