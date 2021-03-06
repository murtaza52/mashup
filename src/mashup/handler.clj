;; ### Ring Handlers
;; This ns defines the ring handlers.

(ns mashup.handler
  (:use [compojure.core :only [GET ANY defroutes]]
        [mashup.remote :only [fetch-data]]
        [ring.util.response :only [file-response]]
        [midje.sweet :only [facts fact]]
        [mashup.utils.request :only [mock-req]])
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [shoreleave.middleware.rpc :as rpc]))


;; In the current scheme the page is rendered three times -
;; 1. Page Request - The initial display when the page is first requested. This
;; displays the body of the index page that was sent over.
;; 2. JS Load - The client side javascript has loaded and has executed.
;; At this point the webfui framework replaced the body with the
;; generated output.
;; 3. Data Fetched - The data requested from the remote api arrives,
;; which causes a rerendering of the DOM.
;;
;; The displays in the 1 and 2 are similar, thus the user does not
;; precieve the refresh.

;; The body of the index page has the initial content and spinner that
;; will be displayed when the user request's the page.

;; The index page is an .html file in the file system that is served
;; using ring's file-response. The :root option is provided to specify
;; the root directory.

(defn index
  "Serves the index page from the file system."
  []
  (file-response "templates/index.html" {:root "resources/public"}))

(defroutes app-routes
  (GET "/" [] (index))
  (route/resources "/"))

(def app (-> app-routes
             (rpc/wrap-rpc "/_shoreleave") ;; wrapping the routes with the shoreleave handler. Any requests to the _shoreleave url will be handled by the handler
             handler/site))

;; Mock request fn for testing.
(def req (mock-req app))

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
