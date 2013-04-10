;; ## State Atom

(ns mashup.state)

;; The atom stores the state of the application. This atom can have
;; any arbitrary value. The atom is passed to the rendering function
;; which builds the DOM. Any changes in the state causes the rendering
;; function to recreate the DOM, and any deltas are rerendered by the
;; webfui framework.

(def app-state (atom {:data nil :tw-uname nil}))
