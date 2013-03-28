(ns mashup.remote
  (:use [mashup.utils :only [log]]
        [mashup.crossover.state :only [app-state]])
  (:require [shoreleave.remotes.http-rpc :as rpc])
  (:require-macros [shoreleave.remotes.macros :as macros]))

(defn fetch-data
    [dt-type]
    (rpc/remote-callback :fetch-data [dt-type]
                         (fn [data]
                           (log "data-received" data)
                           (swap! app-state
                                  (fn
                                    [{:keys [tw-uname]}]
                                    {:data data :tw-uname tw-uname})))))

(defn get-twitter-uname
    []
    (rpc/remote-callback :get-twitter-uname []
                         (fn [uname]
                           (log "name-received" uname)
                           (swap! app-state
                                  (fn
                                    [{:keys [data]}]
                                    {:data data :tw-uname uname})))))
