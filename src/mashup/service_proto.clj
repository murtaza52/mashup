;; ### Service Protocol
;; The protocol defines two methods -
;; 1) fetch for fetching of the data.
;; 2) parse for parsing of the fetched data.

(ns mashup.service-proto)

(defprotocol Service
  (fetch [this] "A way to connect to the API and fetch data")
  (parse [this data] "A way to parse the received data, and return it in a format for the data to be displayed."))

