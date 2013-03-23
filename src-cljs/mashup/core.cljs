(ns mashup.core
  (:use [webfui.framework :only [launch-app]]
        [webfui.utilities :only [get-attribute clicked]]
        [mashup.utils :only [log set-repl]])
  (:use-macros [webfui.framework.macros :only [add-dom-watch add-mouse-watch]])
  (:require [shoreleave.remotes.http-rpc :as rpc]
            [domina :as dom])
  (:require-macros [shoreleave.remotes.macros :as macros]))

(set-repl)

(def app-state (atom {:data nil :tw-uname nil}))

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

(def dt-types [["Day" :dt-type :day] ["Month" :dt-type :month] ["Year" :dt-type :year]])

(defn create-buttons
  [btns]
  [:div.btn-group.bs-docs-sidenav.affix.btn-top-margin {:data-toggle "buttons-radio"}
   (for [[text mouse group-by] btns]
     [:button.btn.btn-primary {:type :button :mouse mouse :group-by group-by :id text} text])])

(def spinner [:div.row-fluid
              [:div.span6.offset4
               [:img {:src "/img/ajax-loader.gif"}]]])

(defn heading
  [text]
  [:div.row-fluid
   [:div.span12.page-header.title-color
    [:h1 text]]])

(defn twitter-div
  [item]
  [:div (str "Tweeted on " (item :day))
   [:blockquote
    [:p (str (item :text))]
    [:small tw-uname]]])

(defn github-div
  [item]
  [:div
   [:code "Github Activity"]
   (str " on " (item :day))
   [:p
    [:span.muted (item :type)]
    [:span " in repo "]
    [:em (str " "  (item :repo))]]])

(defn dt-title
  [text]
  [:div.row-fluid
   [:div.span12
    [:h2.title-color text]]])

(defn render-all
  [{:keys [data tw-uname]}]
  [:div.container-fluid
   [:div.row-fluid
    [:div.span2
     (create-buttons dt-types)]
    [:div.span10
     (heading "Mashup Generator")
     (if (nil? data)
       spinner
       (for [[dt-group items] data]
         [:div
          (dt-title dt-group)
          (for [item items
                :let [source (item :source)]]
            [:div.row-fluid
             [:div.span12
              (case source
                :twitter (twitter-div item)
                :github (github-div item))]])]))]]
   [:script {:type "text/javascript"}
   "$(document).ready(function(){$('#Day').button('toggle');});"]])

(add-mouse-watch :dt-type [state first-element last-element]
                 (when (clicked first-element last-element)
                   (-> (get-attribute first-element :group-by)
                       (fetch-data))
                   {:data nil}))

(fetch-data :day)

(get-twitter-uname)

(launch-app app-state render-all)

(js/setButton)


