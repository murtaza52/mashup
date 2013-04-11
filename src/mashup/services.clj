
(ns mashup.services
  (:use [clojure.pprint :only [pprint]]
        [midje.sweet :only [fact facts]]))

;; ### Services Atom
;; The atom is a vector of vector. Each inner vector contains n number of func.
;; The functions are defined in the sequence of their execution, with the output of the first passed as the input to the second.

(def services (atom []))

;; ex usage of the fn (add-service [1 [inc inc inc]])

(defn add-service
  "conjs the input coll to the atom vector."
  [coll]
  (swap! services conj coll))

(defn exec-services
  "Executes each coll of fns as a future, and concatenates the result."
  ([] (exec-services @services))
  ([v]
     (->>
      (map (fn [coll]
              (future ((apply comp (reverse coll)))))
            v)
      (map deref)
      (apply concat)
      doall))) ;;using doall to force the lazy seq generated by map
