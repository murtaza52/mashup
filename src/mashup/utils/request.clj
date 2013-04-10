(ns mashup.utils.request
  (:use [ring.mock.request :only [request]]
        [midje.sweet :only [facts fact]]))

(defn mock-req
  "Utility function for creating a mock request"
  [app-routes]
  (fn [[verb url & params]]
    (app-routes (request verb url (first params)))))
