;; ### Remote API's
;; This ns exposes the API calls that can be remotely called from the
;; client. It uses the shoreleave library's defremote macro to expose
;; the remote calls. The defremote macro wraps any functions and exposes
;; them at the _shoreleave url.

(ns mashup.remote
  (:use [shoreleave.middleware.rpc :only [defremote]]
        [mashup.core :only [fetch-it! post-process]]
        [midje.sweet :only [facts fact]]
        [clojure.algo.generic.functor :only [fmap]]
        [mashup.utils.collection :only [dissoc-date-from-seq-of-maps]])
  (:require [mashup.config :as config]))


;; The function below uses the diamond wand (-<>), from the swiss-arrows lib. It has the default behavour of ->, however the threading position can optionally be fdefined using the <> symbol. This is done in the last form.

;; The function below does the following -
;; 1) Retrieve the data from the services.
;; 2) Group the data based on the given dt-key.
;; 3) Remove any date instances from the data (As they cause an error on the client side).
;; 4) Sort the maps.
;; 5) Convert the data into a vector. The structure of the data is  - [ ["2012" [{:a 2} {:b 3}]] ["2013" [{:a 2} {:b 3}]] ]
;; In each vector the first element represents the date by which it was grouped, while the the second element is a vector of maps. Each map representing the item that was retrieved.

(defremote fetch-data
  "Returns the data retrieved from services as a vector of vectors."
  [dt-key]
  (->>
   (fetch-it!) ;; fetch the data from external io, and pre process it.
   (post-process dt-key) ;; process the data based on the dt-key
   (fmap dissoc-date-from-seq-of-maps) ;; fmap is used as it applies the f each value of the hash-map and preserves the structure.
   (into [])))

;; (facts "Checking the fetched data"
;;        (let [data (fetch-data :month)]
;;          (fact "The data structure is a vector"
;;                data => vector?)

;;          ;; (fact "The data structure has no date instances"
;;          ;;       data => (fn [d]
;;          ;;                 (walk date? #(every? true? %) d)))
;;          ))

;;(fetch-data)
;(-> (fetch-data :day) pprint)

;; (facts "Checking the returned data"
;;        (fact "The data is free of any instance of org.joda.time.DateTime"
;;              {:ab {:a 2 :b 4}} => (fn [data]
;;                                       (every? (fn [[k v]] (not= (type v) org.joda.time.DateTime))
;;                                               (-> data vals first first))))
;;        (fact "The returned data is grouped"
;;              (fetch-data :day) => #(-> % keys empty? not)))

(defremote get-twitter-uname
  []
  config/tw-screen-name)

(fact "Returns a string"
      (get-twitter-uname) => #(-> % type (= java.lang.String)))
