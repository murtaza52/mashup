(ns mashup.remote
  (:use [shoreleave.middleware.rpc :only [defremote]]
        [mashup.mashit :only [fetch-it!]]
        [midje.sweet :only [facts fact]]
        [clojure.set :only [subset? difference]])
  (:require [mashup.config :as config]))

(def data (fetch-it!))

(defn group-data
  [data dt-type]
  (group-by #(%1 dt-type)
            data))

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

(defn remove-date
  [kv]
  (map #(dissoc %1 :time)
       kv))

(fact "Checking :time has been removed"
      (remove-date [{:time 1 :a 2} {:c 3 :time 4}]) => #(->> (reduce merge {} %) (contains? :time) not))

(defremote fetch-data
  [dt-type]
  (->
   data
   (remove-date)
   (group-data dt-type)))

(facts "Checking the returned data"
       (fact "The data is free of any instance of org.joda.time.DateTime"
             (fetch-data :month) => (fn [data]
                                      (every? #(not (instance? org.joda.time.DateTime %)) (-> (fetch-data :month) first val first vals))))
       (fact "The returned data is grouped"
             (fetch-data :day) => #(-> % keys empty? not)))

(defremote get-twitter-uname
  []
  config/tw-screen-name)

(fact "Returns a string"
      (get-twitter-uname) => #(-> % type (= java.lang.String)))



























































