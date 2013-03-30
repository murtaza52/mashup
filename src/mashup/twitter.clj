(ns mashup.twitter
  (:use [twitter.oauth :only [make-oauth-creds]]
        [twitter.api.restful :only [statuses-user-timeline]]
        [midje.sweet :only [facts fact anything]]
        [clj-time.core :only [date-time]]
        [clojure.set :only [difference]]
        [mashup.utils :only [parse-date]])
  (:require [oauth.client :as oauth]
            [mashup.config :as c]
            [mashup.service-proto :as proto]))

(def tw-date (parse-date "E MMM dd HH:mm:ss Z YYYY"))

(fact "Able to parse the date received from twitter"
      (tw-date "Mon Feb 25 02:42:27 +0000 2013") => (date-time 2013 02 25 02 42 27))

(defn make-creds
  []
  (make-oauth-creds c/tw-consumer-key
                    c/tw-consumer-secret
                    c/tw-access-token
                    c/tw-access-secret))

(fact "Returns an object of type twitter.oauth.OauthCredentials"
      (make-creds) => #(-> % type (= twitter.oauth.OauthCredentials)))

(defn tw-fetch [oauth-creds]
  (statuses-user-timeline :oauth-creds oauth-creds))

(defn tw-parse [tweets]
  (map (fn[tweet]
         {:source :twitter
          :text (:text tweet)
          :time (tw-date (:created_at tweet))})
       (:body tweets)))

(facts "Retreiving and parsing of tweets"
       (let [tweets (tw-fetch (make-creds))]
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
                                      (-> parsed-tweets first :time type (= org.joda.time.DateTime))))))

;; The twitter service object

(def twitter
  (reify
    proto/Service
    (fetch [this] (-> (make-creds) tw-fetch))
    (parse [this data] (tw-parse data))))














































