(ns mashup.mashit
  (:use [mashup.github :only [github]]
        [mashup.twitter :only [twitter]]
        [clojure.pprint :only [pprint]]
        [clj-time.core :only [date-time year month day hour minute sec milli]]
        [midje.sweet :only [facts fact]]
        [clojure.set :only [difference]])
  (:require [mashup.config :as c]
            [mashup.service-proto :as p]))

(def services [twitter github])

(defn get-data []
  (->>
   (map #(future
            (->> (p/fetch %)
                 (p/parse %)))
        services)
   (map deref)
   (apply concat)))

(def date-fns {:day [day month year] :month [month year] :year [year]})

(def dt-types [:day :month :year])

(defn floor-date
  [dt-type dt]
  (->> (dt-type date-fns)
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
  [dt-type dt]
  (let [fns (dt-type date-fns)]
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
             (get-date-string :day (date-time 2013 5 12)) => "12-5-2013"))

(defn get-dates-by-type
  [dt-type date]
  (->>
   date
   (floor-date dt-type)
   (get-date-string dt-type)
   (hash-map dt-type)))

(facts "Given a date and type floor it and return its string represetation as a map"
      (let [v (get-dates-by-type :month (date-time 2013 12 5 6 12 21))]
        (fact "The return value is a map"
              v => map?)
        (fact "It has a count of one"
              (count v) => 1)
        (fact "The key is of the given dt-type"
              (-> v keys first) => :month)
        (fact "The type of the value is string"
              (-> v vals first type) => java.lang.String)))

(defn add-date-for-types
  [{:keys [time] :as item}]
  (->>
   (map #(get-dates-by-type % time) dt-types)
   (reduce merge)
   (merge item)))

(fact "Given a map with a time key, it returns back a map with dates for all the keys."
      (-> {:time (date-time 2012 7 26 21 21 45)} add-date-for-types keys) => #(empty?
                                                                         (difference (set dt-types) (set %))))

(defn fetch-it!
  []
  (->> (get-data) (map add-date-for-types)))

(fetch-it!)
















































