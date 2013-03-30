;; ### Remote AJAX calls
;;
;; Remote ajax calls are made using shoreleave which builds upon fetch.
;; Shoreleave adds additional security to fetch by ensuring that tokens
;; are sent in each request to protect against CSRF attacks.

(ns mashup.remote
  (:use [mashup.utils :only [log]]
        [mashup.state :only [app-state]])
  (:require [shoreleave.remotes.http-rpc :as rpc])
  (:require-macros [shoreleave.remotes.macros :as macros]))

;; The remore-callback macro, takes three parameters the first one being a
;; symbol which represnets the name of the remote function (exposed
;; using defremote on the server side), a vector of params which the
;; rempte function will be called with and an anonymous function which
;; is called with the result of the ajax call.
;;
;;
;; The below function fetches the data retrieved from the github and
;; twitter services, based on the date type grouping. The fetched
;; data is then set in the state atom.
;;
;; Ideally the data should be retrieved just once and then the grouping
;; should be done on the client side. However in this case the grouping
;; depends upon clj-time whose namespaces can not be utilized in cljs.
;;
;; Thus whenever the user wants to see the data grouped by a different
;; date type - day, month or year - a call is made to the server with
;; that type


(defn fetch-data
    [dt-type]
    (rpc/remote-callback :fetch-data [dt-type]
                         (fn [data]
                           (log "data-received" data)
                           (swap! app-state
                                  (fn
                                    [{:keys [tw-uname]}]
                                    {:data data :tw-uname tw-uname})))))

;; The below function fetches the user's twitter sceen name. The result
;; is set in the state atom.

(defn get-twitter-uname
    []
    (rpc/remote-callback :get-twitter-uname []
                         (fn [uname]
                           (log "name-received" uname)
                           (swap! app-state
                                  (fn
                                    [{:keys [data]}]
                                    {:data data :tw-uname uname})))))
