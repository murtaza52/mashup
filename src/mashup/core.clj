;; The code in this ns retrieves the data in parallel for both the
;; services - github and twitter.
;; The data is then processed to include date for each item in three
;; formats - day, month and year.

(ns mashup.core
  (:use [mashup.github :only [github]]
        [mashup.twitter :only [twitter]]
        [clojure.pprint :only [pprint]]
        [clj-time.core :only [date-time year month day hour minute sec milli]]
        [midje.sweet :only [facts fact]]
        [clojure.set :only [difference]]
        [mashup.services :only [exec-services]])
  (:require [mashup.config :as c]))

;; ### Date Processing
;; The functions below deal with the processing of the date for each
;; item.

;; Functions to be applied to create a string representation of the date.

(def to-string-date-fns {:day [month day year] :month [month year] :year [year]})

;; Functions to be applied to floor the date.

(def floor-date-fns {:day [day month year] :month [month year] :year [year]})

;; The date types which will be added to each item.

(def dt-types [:day :month :year])

(defn floor-date
  "Floors the given date based on the date type."
  [dt-type dt]
  (->> (dt-type floor-date-fns)
       (reverse)
       (map #(% dt))
       (apply date-time)))

(facts "Floors the given date to the given type"
       (fact "Floors it to the year"
             (floor-date :year (date-time 2013 5 12 23 13)) => (date-time 2013))
       (fact "Floors it to the month"
             (floor-date :month (date-time 2013 5 12 23 13)) => (date-time 2013 5))
       (fact "Floors it to the day"
             (floor-date :day (date-time 2013 5 12 23 13)) => (date-time 2013 5 12)))

(defn get-date-string
  "Creates a string representation of the date based on the given date and date type."
  [dt-type dt]
  (let [fns (dt-type to-string-date-fns)]
    (->>
     (map #(%1 dt) fns)
     (interpose "-")
     (apply str))))

(facts "Converts the given date into string"
       (fact "Returns a date string for the year"
             (get-date-string :year (date-time 2013 5 12)) => "2013")
       (fact "Returns a date string for the month"
             (get-date-string :month (date-time 2013 5 12)) => "5-2013")
       (fact "Returns a date string for the day"
             (get-date-string :day (date-time 2013 5 12)) => "5-12-2013"))

(defn floor-and-string-by-type
  "Floor the given date and return its string representation"
  [dt-type date]
  (->>
   date
   (floor-date dt-type)
   (get-date-string dt-type)
   (hash-map dt-type)))

(facts "Given a date and type floor it and return its string represetation as a map"
      (let [v (floor-and-string-by-type :month (date-time 2013 12 5 6 12 21))]
        (fact "The return value is a map"
              v => map?)
        (fact "It has a count of one"
              (count v) => 1)
        (fact "The key is of the given dt-type"
              (-> v keys first) => :month)
        (fact "The type of the value is string"
              (-> v vals first type) => java.lang.String)))

(defn add-date-for-types
  "assoc a string representation of date for each date type with the item."
  [{:keys [time] :as item}]
  (->>
   (map #(floor-and-string-by-type % time) dt-types)
   (reduce merge)
   (merge item)))

(fact "Given a map with a time key, it returns back a map with dates for all the keys."
      (-> {:time (date-time 2012 7 26 21 21 45)} add-date-for-types keys) => #(empty?
                                                                               (difference (set dt-types) (set %))))

;; #### Fetch the data
;; The data is fetched and stored in an atom. This is to prevent multiple
;; calls to the server when the data is requested multiple times by the
;; client.

(def retrieved-data (atom nil))

(defn fetch-it!
  [fetch-again?]
  (when (or fetch-again?
            (nil? @retrieved-data))
    (->> (exec-services)
         (map add-date-for-types)
         (reset! retrieved-data)))
  @retrieved-data)
