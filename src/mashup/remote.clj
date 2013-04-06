;; ### Remote API's
;; This ns exposes the API calls that can be remotely called from the
;; client. It uses the shoreleave library's defremote macro to expose
;; the remote calls. The defremote macro wraps any functions and exposes
;; them at the _shoreleave url.

(ns mashup.remote
  (:use [shoreleave.middleware.rpc :only [defremote]]
        [mashup.core :only [fetch-it!]]
        [midje.sweet :only [facts fact]]
        [mashup.utils :only [dissoc-date-from-seq-of-maps sort-map-by-date group-by-key get-val-from-first]]
        [clojure.pprint :only [pprint]]
        [swiss-arrows.core :only [-<>]]
        [clojure.algo.generic.functor :only [fmap]])
  (:require [mashup.config :as config]))

(def get-time-value (partial get-val-from-first :time))

(fact "Given [{:a 2 :time 3} {:time 4 :a 5}] returns the :time from the first map."
      (get-time-value [{:a 2 :time 3} {:time 4 :a 5}]) => 3)


(def sorter (partial sort-map-by-date get-time-value))

;; The function below uses the diamond wand (-<>), from the swiss-arrows lib. It has the default behavour of ->, however the threading position can optionally be fdefined using the <> symbol. This is done in the last form.
(defremote fetch-data
  [dt-type]
  (-<>
   (fetch-it! false)
   (group-by-key dt-type)
   sorter
   (fmap dissoc-date-from-seq-of-maps <>)))

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
