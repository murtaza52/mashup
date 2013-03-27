;; ### Ring Hadlers
;; This ns defines the ring handlers.

(ns mashup.handler
  (:use [compojure.core :only [GET ANY defroutes]]
        [ring.mock.request :only [request]]
        [mashup.remote :only [fetch-data]]
        [ring.util.response :only [file-response]]
        [midje.sweet :only [facts fact]])
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [shoreleave.middleware.rpc :as rpc]))

(defn req
  "Utility function for creating a mock request"
  [[verb url & params]]
  (app (request verb url (first params))))

(defn index
  "The index page."
  []
  (file-response "templates/index.html" {:root "resources/public"}))

(defroutes app-routes
  (GET "/" [] (index))
  (route/resources "/"))

(def app (-> app-routes
             (rpc/wrap-rpc "/_shoreleave")
             handler/site))

(facts "Checking Routes"
       (fact "Compojure Routes : Index Page"
             (req [:get "/"]) => (comp (partial = 200) :status)
             (req [:get "/"]) => (comp (partial = java.io.File) type :body))
       (fact "Shoreleave Route : Checking if shoreleave url exists"
             (req [:post "/_shoreleave"]) => (comp (partial = "Remote not found.") :body))
       (fact "Shoreleave Route : Testing the fetch-data remote"
             (req [:post "/_shoreleave" {:params [:month] :remote "fetch-data"}]) => (comp (partial = 202) :status))
       (fact "Shoreleave Route : Testing the get-twitter-uname remote"
             (req [:post "/_shoreleave" {:remote "get-twitter-uname"}]) => (comp (partial = 202) :status)))



















































