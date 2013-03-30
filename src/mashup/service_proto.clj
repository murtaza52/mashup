;; ### Service Protocol
;; The purpose of the service protocol is to provide an explicit
;; contract that any service will have to implement, if it needs to be
;; integrated in the mashup.

(ns mashup.service-proto)

;; The protocol defines two methods -
;; 1) fetch for fetching of the data.
;; 2) parse for parsing of the fetched data.

(defprotocol Service
  (fetch [this] "A way to connect to the API and fetch data")
  (parse [this data] "A way to parse the received data, and return it in a format for the data to be displayed."))

