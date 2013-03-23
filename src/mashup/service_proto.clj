(ns mashup.service-proto)

(defprotocol Service
  (fetch [this] "A way to connect to the API and fetch data")
  (parse [this data] "A way to parse the received data, and return it in a format for the data to be displayed."))

