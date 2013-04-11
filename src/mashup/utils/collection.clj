;; This ns contains generic functions that are used in the app.
;;
;; The purpose of most of these fns is to separate the means of combination and abstraction from the subroutines themselves - (Yes SICP !)

;; Many of these abstractions may not be generic enough to be reused, however they still serve a very important purpose as they enable the separation of means of the combination from the subroutine itself. Thus it provides clarity to what the fn's purpose is and makes testing a breeze.

(ns mashup.utils.collection
  (:use [midje.sweet :only [facts fact]]
        [clojure.set :only [map-invert difference]]
        [clojure.algo.generic.functor :only [fmap]]
        [clj-time.core :only [date-time]]))

(defn free-of-type?
  "Returns a fn that traverses through the coll and applies the pred fn to each element. Returns true if and oly if the pred is false for each element. Also tests on values of embedded hash-maps. "
  [pred]
  (fn [coll]
    (every? (complement pred)
        (remove coll?
                (tree-seq coll? #(if (map? %)
                                   (vals %)
                                   %)
                          coll)))))

(fact "Returns a fn which checks for strings"
      ((free-of-type? string?) [[2012 [{:a 2} {:b 3}]] [2013 [{:a 2} {:b 3}]]]) => true
      ((free-of-type? string?) [["hello" [{:a 3} {:b 3}]] [2013 [{:a 2} {:b 3}]]]) => false
      ((free-of-type? string?) [[2111 [{:a "hi"} {:b 3}]] [2013 [{:a 2} {:b 3}]]]) => false)

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

(defn mapz
  "maps f to each value in the keys collection ks. zips the result, with the key."
  [f ks]
  (fn [& params]
    (->> (map #(f params %) ks)
         (zipmap ks))))

(fact "maps f to the coll and returns the zipped results."
      ((mapz (fn [[p] k]
               [p k])
             [1 2 3])
       5) => {3 [5 3] 2 [5 2] 1 [5 1]})

(defn group-by-key
  "Given a collection of maps, returns a collection with maps grouped using the value of the supplied key."
  [coll k]
  (group-by #(%1 k) coll))

(group-by-key [{:a 1 :b 2} {:a 3 :b 4} {:a 1 :b 5}] :a)

(fact "Groups the coll of maps by the given key"
      (group-by-key [{:a 1 :b 2} {:a 3 :b 4} {:a 1 :b 5}] :a) => {1 [{:a 1, :b 2} {:a 1, :b 5}], 3 [{:a 3, :b 4}]})

(defn dissoc-with-pred
  "Returns a fn which dissoc any kv for which the pred is true."
  [pred]
  (fn [m]
    (into {} (filter (complement (fn [[k v]] (pred k v))) m))))

(fact "returns a map with the keys dissoc where the pred is true"
      (let [greater-than-2 (dissoc-with-pred (fn [k v] (> v 2)))]
        (greater-than-2 {:a 3 :b 1 :c 7 :d 2}) => {:b 1 :d 2}))

(defn fmerge
  "applies f to a hash-map and merges the result with the input hash-map. f should return a hash-map. If the input is a coll of hash-maps, then maps over it."
  [f coll]
  (if (map? coll)
    (->> (f coll) (merge coll))
    (map #(->> (f %) (merge %))
         coll)))

(facts "applies f and returns the merged map."
       (fact "works with hash-maps"
             (fmerge #(->> (:a %) inc (hash-map :a-inc)) {:a 1 :b 2}) => {:a-inc 2 :a 1 :b 2})
       (fact "works with a coll of hash-maps"
             (fmerge #(->> (:a %) inc (hash-map :a-inc)) [{:a 1 :b 2} {:a 1 :b 2}]) => [{:a-inc 2 :a 1 :b 2}{:a-inc 2 :a 1 :b 2}]))
