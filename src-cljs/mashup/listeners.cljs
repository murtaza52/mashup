(ns mashup.listeners
  (:use [webfui.utilities :only [get-attribute clicked]]
        [mashup.remote :only [fetch-data]])
  (:use-macros [webfui.framework.macros :only [add-mouse-watch]]))

;; ### The add-mouse-watch macro
;;
;; The below function gets called whenever there is a mouse event for an
;; element with the 'mouse' attribute as :change-grouping
;;
;; The webfui framework passess the first element (element on which the
;; mouse down event takes place) and second element (element on which
;; the mouse up event takes place).
;;
;; Also the elements are passed as opposed to events.
;;
;; In the case where there is a click event as opposed to a drag event,
;; the first and second element are same. The clicked macro helps to
;; identify this.
;;
;; The return value of the function is patched with the state atom. Thus
;; the user only returns any changes intended for the state, which is
;; then reflected back to the DOM due to the bindings.

;; The below callback first retrieves the value of the custom attribute
;; :dt-type. It then calls the remote api fetch-data. The fetch-data
;; function will fetch the data and swap! the value in the state atom.
;; The function below returns {:data nil}, this will cause the dom to
;; display a spinner.

;; (add-mouse-watch :change-grouping [state first-element last-element]
;;                  (js/alert "Hello"))

;; (when (clicked first-element last-element)
;;                    (-> (get-attribute first-element :dt-type)
;;                        (fetch-data))
;;                    {:data nil})

(defn add-listener
  []
  (add-mouse-watch :change-grouping [state first-element last-element]
                   (when (clicked first-element last-element)
                     (-> (get-attribute first-element :dt-type)
                         (fetch-data))
                     {:data nil})))
