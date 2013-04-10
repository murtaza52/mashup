;; ### CLJS Utilities
;; This ns contains utility functions for clojurescript development.

(ns mashup.utils
  (:require [clojure.browser.repl :as repl]))

;; Atom to store the mode, by default the mode is set to dev.

(def state (atom {:mode :dev}))

;; The log function helps in printing both strings and javascript objects.
;; The code was inspired from Chris Ganger's waltz lib.

(defn log [msg data]
  "The function takes a msg and data as input. They are appended and printed
  on the browser's js console"
  (when (and js/console
             (= (@state :debug) :dev)
    (let [d (if (string? data)
              data
              (pr-str data))
          s (str msg " :: " d)]
      (.log js/console s)))))

;; The repl is only avalaible in the dev mode. This is to pevent errors
;; in unsupported browsers such as IE in production.

(defn set-repl []
  (when (and js/console (= (@state :debug) :dev))
    (repl/connect "http://localhost:9000/repl")))
