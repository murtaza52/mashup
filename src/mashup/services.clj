
(ns mashup.services)

;; ### Services Atom
;; The atom is a vector of vector. Each inner vector contains n number of func.
;; The functions are defined in the sequence of their execution, with the output of the first passed as the input to the second.

(def services (atom []))

(defn add-service
  [fns]
  (swap! services conj fns))

(defn exec-services
  ([] (exec-services @services))
  ([v] (->> v
                (map (fn [[config fns]]
                       (future ((apply comp (reverse fns)) config))))
                (map deref)
                (apply concat))))
;; (fact "C"
;;       (let [s [[[1] [inc]]
;;                [[2 ]]]]))
