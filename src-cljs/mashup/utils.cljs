(ns mashup.utils
  (:require [clojure.browser.repl :as repl]))

(def state (atom {:debug true}))

(defn log [msg data]
  (when (and js/console
             (@state :debug))
    (let [d (if (string? data)
              data
              (pr-str data))
          s (str msg " :: " d)]
      (.log js/console s))))

(defn set-repl []
  (when (and js/console (@state :debug))
    (repl/connect "http://localhost:9000/repl")))
