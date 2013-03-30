(ns mashup.utils
  (:use [clj-time.format :only [parse formatter formatters]]
        [ring.mock.request :only [request]]))

(defn parse-date
  "Returns a date parser based on the formatter"
  [format]
  (fn [date-string]
    (if (keyword? format)
      (parse (formatters format) date-string)
      (parse (formatter format) date-string))))

(defn mock-req
  "Utility function for creating a mock request"
  [app-routes]
  (fn [[verb url & params]]
    (app-routes (request verb url (first params)))))
