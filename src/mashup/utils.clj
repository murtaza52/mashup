(ns mashup.utils
  (:use [clj-time.format :only [parse formatter formatters]]
        [ring.mock.request :only [request]]
        [clj-time.core :only [date-time year month day before?]]
        [midje.sweet :only [facts fact]]
        [clojure.set :only [map-invert difference]]
        [clojure.algo.generic.functor :only [fmap]]))

(defn parse-date
  "Returns a date parser based on the formatter"
  [format]
  (fn [date-string]
    (if (keyword? format)
      (parse (formatters format) date-string)
      (parse (formatter format) date-string))))

;; ### Date Processing
;; The functions below deal with the processing of the date for each
;; item.

;; Functions to be applied to create a string representation of the date.

(def to-string-date-fns {:day [month day year] :month [month year] :year [year]})

;; Functions to be applied to floor the date.

(def floor-date-fns {:day [day month year] :month [month year] :year [year]})

(defn floor-date
  "Floors the given date based on the date type."
  [dt-type dt]
  (->> (dt-type floor-date-fns)
       (reverse)
       (map #(% dt))
       (apply date-time)))

(facts "Floors the given date based on the given type"
       (fact "Floors it to the year"
             (floor-date :year (date-time 2013 5 12 23 13)) => (date-time 2013))
       (fact "Floors it to the month"
             (floor-date :month (date-time 2013 5 12 23 13)) => (date-time 2013 5))
       (fact "Floors it to the day"
             (floor-date :day (date-time 2013 5 12 23 13)) => (date-time 2013 5 12)))

(defn stringify-date
  "Creates a string representation of the date based on the given date and date type."
  [dt-type dt]
  (let [fns (dt-type to-string-date-fns)]
    (->>
     (map #(%1 dt) fns)
     (interpose "-")
     (apply str))))

(facts "Converts the given date into string"
       (fact "Returns a date string for the year"
             (stringify-date :year (date-time 2013 5 12)) => "2013")
       (fact "Returns a date string for the month"
             (stringify-date :month (date-time 2013 5 12)) => "5-2013")
       (fact "Returns a date string for the day"
             (stringify-date :day (date-time 2013 5 12)) => "5-12-2013"))

(defn floor-and-string
  "Floor the given date and return its string representation"
  [dt-type date]
  (->>
   date
   (floor-date dt-type)
   (stringify-date dt-type)))

(facts "Given a date and type floor it and return its string represetation as a map"
      (let [v (floor-and-string :month (date-time 2013 12 5 6 12 21))]
        (fact "The type of the value is string"
              (type v) => java.lang.String)))

;; The date types which will be added to each item.
(def dt-types [:day :month :year])

(defn add-date-strings
  "assoc a string representation of date for each date type with the item."
  [m date]
  (->> (map #(floor-and-string % date) dt-types)
       (zipmap dt-types)
       (merge m)))

(fact "Given a map with a time key, it returns back a map with dates for all the dt-types."
      (-> (add-date-strings {:a 2} (date-time 2012)) keys) => #(empty?
                                                                       (difference (set dt-types) (set %))))

(defn mock-req
  "Utility function for creating a mock request"
  [app-routes]
  (fn [[verb url & params]]
    (app-routes (request verb url (first params)))))


;; ### Utility Functions
;; Functions for grouping, sorting and removing date instances from the data.

(defn group-by-key
  "Given a collection of maps, returns a collection with maps grouped using the value of the supplied key."
  [data key]
  (group-by #(%1 key) data))

;; ;; Test if the data is grouped correctly.
;; (facts "Data is grouped by the given keys."
;;        (let [grouped-data (group-data (fetch-it! false) :month)
;;              months (map :month (fetch-it! false))
;;              grouped-by-keys (keys grouped-data)]
;;          (fact "The grouping keys are a subset of the :month/:day keys in data"
;;                [months grouped-by-keys] => (fn [[s1 s2]]
;;                                              (subset? (set s1) (set s2))))
;;          (fact "All the grouping keys are distinct"
;;                grouped-by-keys => #(apply distinct? %))
;;          (fact "All the distinct keys from data are present in grouping keys"
;;                [months grouped-by-keys] => (fn [[s1 s2]]
;;                                              (empty? (difference (set s1) (set s2)))))))

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

(defn sort-map-by-value
  "Given a map return a sorted map, in which the sort is done on the map's values, instead of keys.
   Takes a function as an input, which will be used  for sorting
   cf -> The comparator function
   kf -> The key function which returns the value that is the be compared
   m -> The map which has to be sorted"
  [cf]
  (fn [kf m]
    (->> m
       map-invert
       (into (sorted-map-by #(cf (kf %1) (kf %2))))
       map-invert)))

(def sort-map-by-date (sort-map-by-value date-time-comparator))

;; (fact "Given an input as { 'ab' [ {:time (date-time ...)..} .. ] ..} sorts the map based on the time key of the first element."
;;       (sort-map-by-date {"3-19-2013" [{:time (date-time 2013 3 19 12 14 45)}]
;;                          "3-9-2013" [{:time (date-time 2013 3 9 16 46 49)}]
;;                          "2-25-2013" [{:time (date-time 2013 2 25 2 38 15)}]
;;                          "3-14-2013" [{:time (date-time 2013 3 14 7 19 23)}]
;;                          "2-8-2013" [{:time (date-time 2013 2 8 12 44 47)}]}) => {"3-19-2013" [{:time (date-time 2013 3 19 12 14 45)}]
;;                                                                                   "3-14-2013" [{:time (date-time 2013 3 14 7 19 23)}]
;;                                                                                   "3-9-2013" [{:time (date-time 2013 3 9 16 46 49)}]
;;                                                                                   "2-25-2013" [{:time (date-time 2013 2 25 2 38 15)}]
;;                                                                                   "2-8-2013" [{:time (date-time 2013 2 8 12 44 47)}]})

(defn dissoc-from-seq-of-maps
  [dissoc-map-fn]
  (fn [s]
    (mapv dissoc-map-fn s)))

(def dissoc-date-from-seq-of-maps (dissoc-from-seq-of-maps dissoc-date-time))

(dissoc-date-from-seq-of-maps [{:time (date-time 2012) :e 2} {:f 3 :hey-man (date-time 2111)}])


(fact "Removes all date-time instances from the following data structure {k [{v}]}"
      (fmap dissoc-date-from-seq-of-maps {:a [{:time (date-time 2012) :e 2} {:f 3}]
                    :b [{:time (date-time 2013) :h 5}]}) => (fn [data]
                                                                     (every? (fn [[k v]] not= (type v) org.joda.time.DateTime)
                                                                             (-> data vals first first))))


(defn get-val-from-first
  "Function for extracting the date-time from the value of the given vector of maps."
  [key s]
  (-> s first key))
