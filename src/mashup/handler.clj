(ns mashup.handler
  (:use [compojure.core :only [GET ANY defroutes]]
        [ring.mock.request :only [request]]
        [mashup.remote :only [fetch-data]]
        [ring.util.response :only [file-response]]
        [midje.sweet :only [facts fact]])
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [shoreleave.middleware.rpc :as rpc]))

(defn index []
  (file-response "templates/index.html" {:root "resources/public"}))

(defroutes app-routes
  (GET "/" [] (index))
  (route/resources "/"))

(def app (-> app-routes
             (rpc/wrap-rpc "/_shoreleave")
             handler/site))

(defn req
  [[verb url & params]]
  (app (request verb url (first params))))

(defn apply-fns
  [fns]
  (fn [v0]
    (reduce (fn [v f] (f v)) v0 fns)))

(comment #(-> % :status (= 200)))

(facts "Checking Routes"
       (fact "Compojure Routes : Index Page"
             (req [:get "/"]) => (apply-fns [:status #(= % 200)])
             (req [:get "/"]) => (apply-fns [:body type #(= % java.io.File)]))
       (fact "Shoreleave Route : Checking if shoreleave url exists"
             (req [:post "/_shoreleave"]) => (apply-fns [:body #(= % "Remote not found.")]))
       (fact "Shoreleave Route : Testing the fetch-data remote"
             (req [:post "/_shoreleave" {:params [:month] :remote "fetch-data"}]) => (apply-fns [:status #(= % 202)]))
       (fact "Shoreleave Route : Testing the get-twitter-uname remote"
             (req [:post "/_shoreleave" {:remote "get-twitter-uname"}]) => (apply-fns [:status #(= % 202)])))























































