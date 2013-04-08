(ns mashup.utils.date
  (:use [clj-time.format :only [unparse parse formatter formatters]]
        [clj-time.core :only [default-time-zone date-midnight date-time year month day hour minute sec milli after?]]
        [midje.sweet :only [facts fact]]
        [clojure.set :only [difference]]))

;; Predicate fn to test of the given arg is of type org.joda.time.DateTime.

(defn date?
  "Returns true if the type of the arg is of org.joda.time.DateTime"
  [v]
  (= (type v) org.joda.time.DateTime))

(fact "Returns true if the type of the arg is of org.joda.time.DateTime"
      (date? (date-time 2012 2 3 4)) => true
      (date? "2012") => false
      (date? 2012) => false)

;; Parses a given date given a format and a date. clj-time accepts two types of formatters, string and keywords. The below fn provides a uniform interface for invoking both.

(defn parse-date
  "Returns a date parser based on the formatter"
  [format]
  (fn [date-string]
    (cond
     (keyword? format) (parse (formatters format) date-string)
     (string? format) (parse (formatter format) date-string))))

;; ### Date Processing
;; The functions below deal with the processing of the date for each
;; item.

;; Functions to be applied to create a string representation of the date.

;; (def to-string-date-fns {:day [month day year] :month [month year] :year [year]})

;; ;; Functions to be applied to floor the date.

;; (def floor-date-fns {:day [day month year] :month [month year] :year [year]})

;; (defn floor-date
;;   "Floors the given date based on the date type."
;;   [dt-type dt]
;;   (->> (dt-type floor-date-fns)
;;        (reverse)
;;        (map #(% dt))
;;        (apply date-time)))

;; (facts "Floors the given date based on the given type"
;;        (fact "Floors it to the year"
;;              (floor-date :year (date-time 2013 5 12 23 13)) => (date-time 2013))
;;        (fact "Floors it to the month"
;;              (floor-date :month (date-time 2013 5 12 23 13)) => (date-time 2013 5))
;;        (fact "Floors it to the day"
;;              (floor-date :day (date-time 2013 5 12 23 13)) => (date-time 2013 5 12)))

;; (defn stringify-date
;;   "Creates a string representation of the date based on the given date and date type."
;;   [dt-type dt]
;;   (let [fns (dt-type to-string-date-fns)]
;;     (->>
;;      (map #(%1 dt) fns)
;;      (interpose "-")
;;      (apply str))))

(def date-fn {:day day :month month :year year :hour hour :minute minute :sec sec :milli milli})

(defn get-date-fns
  [k]
  (let [f (k date-fn)]
    (-> (take-while (partial not= f) [year month day hour minute sec milli])
        reverse
        (conj f))))

(defn floor-date
  "Floors the date based on the given dt-type."
  [dt-type date]
  (->> (get-date-fns dt-type)
       (map #(% date))
       reverse
       (apply date-midnight)))

(facts "Floors the given date based on the given type"
       (fact "Floors it to the year"
             (floor-date :year (date-time 2013 5 12 23 13)) => (date-midnight 2013))
       (fact "Floors it to the month"
             (floor-date :month (date-time 2013 5 12 23 13)) => (date-midnight 2013 5))
       (fact "Floors it to the day"
             (floor-date :day (date-time 2013 5 12 23 13)) => (date-midnight 2013 5 12)))

;; Custom formatters that are needed for converting a date into a string. The given clj-time formatter are used where applicable (:year), and string ones are specified where needed (:day and :month).

(def custom-formatters {:day "MM-dd-YYYY" :month "MM-YYYY" :year :year})

(defn unparse-date
  "Returns the string representation of a date given the formatter."
  [format date]
  (cond
   (keyword? format) (unparse (formatters format) date)
   (string? format) (unparse (formatter format) date)))

(facts "Converts the given date into string"
       (fact "Returns a date string for the year"
             (unparse-date (:year custom-formatters) (date-time 2013 5 12)) => "2013")
       (fact "Returns a date string for the month"
             (unparse-date (:month custom-formatters) (date-time 2013 5 12)) => "05-2013")
       (fact "Returns a date string for the day"
             (unparse-date (:day custom-formatters) (date-time 2013 5 12)) => "05-12-2013"))

(defn floor-and-string
  "Floors and returns the string representation given the date and dt-type."
  [[date] dt-type]
  (->> (floor-date dt-type date)
       (unparse-date (dt-type custom-formatters))))

(facts "Given a date and type, floor it and return its string represetation."
       (let [v (floor-and-string [(date-time 2013 12 5 6 12 21)] :month)]
        (fact "The type of the value is string"
              (type v) => java.lang.String)))

(defn date-time-comparator
  "Predicate function for comparing two date-time's"
  [time1 time2]
  (after? time1 time2))

(fact "Returns true if the first date is chronologically after the second date."
      (date-time-comparator (date-time 2013) (date-time 2011)) => true)

(def multi-parser (formatter (default-time-zone) "MM-dd-YYYY" "MM-YYYY" "YYYY"))

(facts "The multi-parse can be used as a formatter to parse dates in day, month and year formats."
       (fact "It can parse a date in YYYY format"
             (parse multi-parser "2012") => date?))

;; (fact "Given a map with a time key, it returns back a map with dates for all the dt-types."
;;       (-> (add-date-strings {:a 2} (date-time 2012)) keys) => #(empty?
;;                                                                 (difference (set dt-types) (set %))))
