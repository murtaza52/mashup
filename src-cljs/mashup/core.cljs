(ns mashup.core
  (:use [webfui.framework :only [launch-app]]
        [mashup.utils :only [log set-repl]]
        [mashup.dom :only [render-all]]
        [mashup.state :only [app-state]]
        [mashup.remote :only [fetch-data get-twitter-uname]]
        [mashup.listeners :only [add-listener]])
  (:require [domina :as dom]))

;; Calling the utility function to set the repl.
;; Currently this doesnt work as webfui replaces the complete body and
;; hence also replaces the repl callback that is included into the body.
;; Will need to patch the webfui lib for this to work.
(set-repl)

;; Fetching the data with the :day provided as the default grouping
;; criteria. Other grouping keys - :month and :year can be provided as needed.
(fetch-data :day)

;; Fetching the user's screen name for twitter. This is used for display purposes.
(get-twitter-uname)

;; Webfui's launch-app function takes the state and a render function.
;; It passess the state in the render function, generates html from
;; the resulting vector (hiccup like dom structure), and replaces the
;; body with the generated html.
;;
;; Webfui also ensures that the state and the dom are in sync. Any
;; changes to the state atom are reflected back in the dom.
(launch-app app-state render-all)

;; calls the javascript function to set the button state.
;; (js/setButton)

;; Add the listener for mouse clicks, for the Day, Month, Year buttons.
(add-listener)
