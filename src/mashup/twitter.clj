;; ### Twitter Service
;; This ns contains the functions for fetching and parsing the data retrieved from twitter.
;; It also defines the config var for adding to the services atom.

(ns mashup.twitter
  (:use [twitter.oauth :only [make-oauth-creds]]
        [twitter.api.restful :only [statuses-user-timeline]]
        [midje.sweet :only [facts fact anything]]
        [clj-time.core :only [date-time]]
        [clojure.set :only [difference]]
        [mashup.utils.date :only [parse-date date?]])
  (:require [oauth.client :as oauth]
            [mashup.config :as c]))

;; fn for parsing the date for each tweet.

(def tw-date (parse-date "E MMM dd HH:mm:ss Z YYYY"))

(fact "Able to parse the date received from twitter"
      (tw-date "Mon Feb 25 02:42:27 +0000 2013") => (date-time 2013 02 25 02 42 27))

;; The twitter configuration for connecting to its API.

(defn tw-config
  []
  (vector c/tw-consumer-key c/tw-consumer-secret c/tw-access-token c/tw-access-secret))

(defn make-cred
  "returns oauth cred that can be used to connect to twitter."
  [c]
  (apply make-oauth-creds c))

(fact "Returns an object of type twitter.oauth.OauthCredentials"
      (make-creds (tw-config)) => #(instance? twitter.oauth.OauthCredentials %))

(defn tw-fetch
  "Fetches tweets of a user given the uath credentials."
  [oauth-creds]
  (statuses-user-timeline :oauth-creds oauth-creds))

(defn tw-parse
  "Parses the tweets into a collection of maps."
  [tweets]
  (map (fn[tweet]
         {:source :twitter
          :text (:text tweet)
          :time (tw-date (:created_at tweet))})
       (:body tweets)))

(facts "Retreiving and parsing of tweets"
       (let [tweets (tw-fetch (make-creds (tw-config)))]
         (fact "The response has a status of 200"
               (-> tweets :status :code) => 200)
         (fact "The tweets are in the body of the response as a vector of maps"
               (-> tweets :body) => #(and (vector? %)
                                          (map? (first %))))
         (fact "Each tweet has the given keys"
               (-> tweets :body first) => (fn [tweet]
                                            (every? #(contains? tweet %) [:text :created_at])))
         (fact "The parsed tweet has source as :twitter"
               (tw-parse tweets) => (fn [parsed-tweets]
                                      (-> parsed-tweets first :source (= :twitter))))
         (fact "The parsed tweet has the given keys"
               (tw-parse tweets) => (fn [parsed-tweets]
                                      (empty? (difference #{:source :text :time} (-> parsed-tweets first keys set)))))
         (fact "The parsed tweet has :time of type org.joda.time.DateTime"
               (tw-parse tweets) => (fn [parsed-tweets]
                                      (-> parsed-tweets first :time date?)))))

;; The config vector that will be used by the exec-services fn.

(def srv [tw-config make-creds tw-fetch tw-parse])
