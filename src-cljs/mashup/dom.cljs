;; ### DOM Construction
;;
;; This ns contains the different functions used for DOM construction.
;; The code in this ns does not have any dependencies and is pure, and can be used
;; on both on the server and client.


(ns mashup.dom)

;; The dt-btns is a vector of vectors, used for creating the buttons.
;; The first value in the vector is used for the text and id, the second
;; is used for setting a custom attribute and the third for setting the
;; listener for a mouse event.
;;
;; The value of the custom attribute is used for storing the param that
;; will be passed to the remote function for retrieving data.

(def dt-btns
  (mapv #(vector %1 %2 %3) ["Day" "Month" "Year"] [:day :month :year] (repeat :change-grouping)))

;; Method for creating the toggle buttons. It first creates a div that
;; can affixed to the side panel, and then loops over a btns vector to
;; create them.

(defn create-buttons
  [btns]
  [:div.btn-group.bs-docs-sidenav.affix.btn-top-margin {:data-toggle "buttons-radio"}
   (for [[text dt-type mouse] btns]
     [:button.btn.btn-primary {:type :button :mouse mouse :dt-type dt-type :id text} text])])


;; Spinner that will be displayed when the data is loading.

(def spinner [:div.row-fluid
              [:div.span6.offset4
               [:img {:src "/img/ajax-loader.gif"}]]])

;; The page heading that will be displayed at the top of the page.

(defn heading
  [text]
  [:div.row-fluid
   [:div.span12.page-header.title-color
    [:h1 text]]])

;; The twitter block. It outputs the tweet in a blockquote with
;; the author's name.

(defn twitter-div
  [item tw-uname]
  [:div (str "Tweeted on " (item :day))
   [:blockquote
    [:p (str (item :text))]
    [:small tw-uname]]])

;; The github block. It outputs the day the github evet took place, the
;; type of event and the repository on which the event took place.

(defn github-div
  [item]
  [:div
   [:code "Github Activity"]
   (str " on " (item :day))
   [:p
   [:span.muted (item :type)]
    [:span " in repo "]
    [:em (str " "  (item :repo))]]])

;; The title for grouping the items by day, month, year.

(defn dt-title
  [text]
  [:div.row-fluid
   [:div.span12
    [:h2.title-color text]]])

;; The function for creating the body. This function will also be passed
;; to the launch-app macro, and will be called whenever the state
;; changes.

(defn render-all
  "Creates the body and takes the state as the input."
  [{:keys [data tw-uname]}]
  [:div.container-fluid
   [:div.row-fluid
    [:div.span2
     (create-buttons dt-btns)]
    [:div.span10
     (heading "Mashup Generator")
     (if (nil? data) ;; Spinner will be diaplyed when there is no data.
       spinner
       (for [[dt-group items] data]
         [:div
          (dt-title dt-group)
          (for [item items
                :let [source (item :source)]]
            [:div.row-fluid
             [:div.span12
              (case source
                :twitter (twitter-div item tw-uname)
                :github (github-div item))]])]))]]
   [:script {:type "text/javascript"}
   "$(document).ready(function(){$('#Day').button('toggle');});"]])



