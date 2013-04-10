(ns mashup.utils.collection
  (:use [midje.sweet :only [facts fact]]
        [clojure.set :only [map-invert difference]]
        [clojure.algo.generic.functor :only [fmap]]
        [clj-time.core :only [date-time]]))


(defn map-sorter
  "Returns a function for sorted-map, given the comparator function, and andother function for processing the key."
  [cf kf]
  (fn [m]
    (into (sorted-map-by
           (fn [k1 k2]
             (cf (kf k1) (kf k2))))
          m)))

(fact "mapsorter returns a fn which sorts a map based on the given comparator and key fns."
      ((map-sorter < identity) {2 :a 1 :b 3 :c}) => {1 :b 2 :a 3 :c})

(defn map-and-zip
  "maps f to each value in the keys collection ks. zips the result, with the key."
  [f ks]
  (fn [& params]
    (->> (map #(f params %) ks)
         (zipmap ks))))

(fact "maps f to the coll and returns the zipped results."
      ((map-and-zip (fn [[p] k]
                [p k])
              [1 2 3])
       5) => {3 [5 3] 2 [5 2] 1 [5 1]})

;; ### Utility Functions
;; Functions for grouping, sorting and removing date instances from the data.

(defn group-by-key
  "Given a collection of maps, returns a collection with maps grouped using the value of the supplied key."
  [coll k]
  (group-by #(%1 k) coll))

(group-by-key [{:a 1 :b 2} {:a 3 :b 4} {:a 1 :b 5}] :a)

(fact "Groups the coll of maps by the given key"
      (group-by-key [{:a 1 :b 2} {:a 3 :b 4} {:a 1 :b 5}] :a) => {1 [{:a 1, :b 2} {:a 1, :b 5}], 3 [{:a 3, :b 4}]})

;; #### Date Removal functionality

(defn dissoc-with-pred
  [f]
  (fn [m]
    (into {} (filter (complement (fn [[k v]] (f k v))) m))))

(fact "returns a map with the keys dissoc where the pred is true"
      (let [greater-than-2 (dissoc-with-pred (fn [k v] (> v 2)))]
        (greater-than-2 {:a 3 :b 1 :c 7 :d 2}) => {:b 1 :d 2}))

;; dissoc any keys with the type of date-time
(def dissoc-date-time
  (dissoc-with-pred (fn [_ v] (= org.joda.time.DateTime (type v)))))

(fact "dissoc any keys having value of type org.joda.time.DateTime"
      (dissoc-date-time {:time (date-time 2013) :a 2 :b "hello"}) => #(every? (fn [[k v]] (not= (type v) org.joda.time.DateTime)) %))

;; #### Sorting Functionality

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


(defn apply-and-merge
  "Applies f to each ahsh-map of the seq s, and then merges both."
  [f]
  (fn [s]
    (map #(-> (f %)
              (merge %))
         s)))

(fact "Applies f and returns the merged map."
      ((apply-and-merge #(->> (:a %) inc (hash-map :a-inc))) [{:a 1 :b 2}]) => [{:a-inc 2 :a 1 :b 2}])
