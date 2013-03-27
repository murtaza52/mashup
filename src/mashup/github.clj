;; ### Github Service
;; The below ns implements the service protocol for the github service.

(ns mashup.github
  (:use [mashup.service-proto]
        [midje.sweet :only [facts fact anything]]
        [clj-time.core :only [date-time]]
        [clojure.set :only [difference]])
  (:require [tentacles.events :as ev]
            [clj-time.format :as time]
            [mashup.config :as c]
            [mashup.twitter :as tw]))

(def gt-date (tw/parse-date :date-time-no-ms))

(fact "Parsing the date received from github"
      (parse-date "2013-02-20T17:24:33Z") => (date-time 2013 02 20 17 24 33))

(defn gt-fetch
  "Fetches the user's events based on the user-name and page number (used for paginating through the events)"
  [user page]
  (ev/performed-events user {:page page}))

(defn gt-parse [events]
  (map (fn [event]
         {:source :github
          :type (:type event)
          :repo (get-in event [:repo :name])
          :time (parse-date (:created_at event))})
       events))

(facts "Fetching and parsing of github events"
       (let [ev ents (gt-fetch c/github-user-name 1)]
         (fact "The events fetched are a vector of maps"
               events => #(and (vector? %) (map? (first %))))
         (fact "Each event has the following keys"
               (first events) => (fn [ev]
                                   (every? #(get-in ev %) [[:type] [:repo :name] [:created_at]])))
         (fact "Parsing an event returns a map of the following structure and with source as github."
               (gt-parse events) => (fn [parsed-events]
                                      (and
                                       (= :github (:source (first parsed-events)))
                                       (empty? (difference #{:source :type :repo :time} (-> parsed-events first keys set))))))
         (fact "Each parsed event has a time of type org.joda.time.DateTime"
               (-> (gt-parse events) first) => #(-> % :time type (= org.joda.time.DateTime)))))

;; The github serice object

(def github
  (reify
    Service
    (fetch [this] (gt-fetch c/github-user-name 1))
    (parse [this data] (gt-parse data))))











































