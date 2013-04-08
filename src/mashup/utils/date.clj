(ns mashup.utils.date
  (:use [clj-time.format :only [unparse parse formatter formatters]]
        [clj-time.core :only [default-time-zone date-midnight date-time year month day hour minute sec milli after?]]
        [midje.sweet :only [facts fact]]
        [clojure.set :only [difference]]))

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

;; (facts "Converts the given date into string"
;;        (fact "Returns a date string for the year"
;;              (stringify-date :year (date-time 2013 5 12)) => "2013")
;;        (fact "Returns a date string for the month"
;;              (stringify-date :month (date-time 2013 5 12)) => "5-2013")
;;        (fact "Returns a date string for the day"
;;              (stringify-date :day (date-time 2013 5 12)) => "5-12-2013"))

;; (defn floor-and-string
;;   "Floor the given date and return its string representation"
;;   [dt-type date]
;;   (->>
;;    date
;;    (floor-date dt-type)
;;    (stringify-date dt-type)))

(def date-fn {:day day :month month :year year :hour hour :minute minute :sec sec :milli milli})

(defn get-date-fns
  [k]
  (let [f (k date-fn)]
    (-> (take-while (partial not= f) [year month day hour minute sec milli])
        reverse
        (conj f))))

(defn floor-date
  [dt-key date]
  (->> (get-date-fns dt-key)
       (map #(% date))
       reverse
       (apply date-midnight)))

(floor-date :year (date-time 2013 11 4 5 6 7))

(defn unparse-date
  "Returns a date parser based on the formatter"
  [format date]
  (cond
   (keyword? format) (unparse (formatters format) date)
   (string? format) (unparse (formatter format) date)))

(def custom-formatters {:day "MM-dd-YYYY" :month "MM-YYYY" :year :year})

(unparse-date (:month custom-formatters) (date-time 2013 11 4 5 6))

;; (facts "Given a date and type floor it and return its string represetation as a map"
;;       (let [v (floor-and-string :month (date-time 2013 12 5 6 12 21))]
;;         (fact "The type of the value is string"
;;               (type v) => java.lang.String)))

(defn floor-and-string
  [[date] k]
  (->> (floor-date k date)
       (unparse-date (k custom-formatters))))


;; (fact "Given a map with a time key, it returns back a map with dates for all the dt-types."
;;       (-> (add-date-strings {:a 2} (date-time 2012)) keys) => #(empty?
;;                                                                 (difference (set dt-types) (set %))))

(defn date-time-comparator
  "Predicate function for comparing two date-time's"
  [time1 time2]
  (after? time1 time2))

(fact "Returns true if the first date is chronologically after the second date."
      (date-time-comparator (date-time 2013) (date-time 2011)) => true)

(def multi-parser (formatter (default-time-zone) "MM-dd-YYYY" "MM-YYYY" "YYYY"))

(parse multi-parser "2012")
