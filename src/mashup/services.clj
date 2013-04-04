
(ns mashup.services
  (:use [clojure.pprint :only [pprint]]
        ;;[midje.sweet :only [fact facts]]
        ))

;; ### Services Atom
;; The atom is a vector of vector. Each inner vector contains n number of func.
;; The functions are defined in the sequence of their execution, with the output of the first passed as the input to the second.

(def services (atom []))

(defn add-service
  [fns]
  (swap! services conj fns))

(defn exec-services
  ([] (exec-services @services))
  ([v]
     (pprint v)
     (->>
            (doall (map (fn [[config fns]]
                          (future ((apply comp (reverse fns)) config)))
                        v))
            (map deref)
            (apply concat))))

;; (facts "C"
;;       (let [s [[1 [inc]]
;;                [2 [inc inc]]]]
;;         (fact "c"
;;               (exec-services [[1 [inc]]
;;                [2 [inc inc]]]) => [2 3])))
