(ns mashup.crossover.dom)

;;
(def dt-types [["Day" :change-grouping :day] ["Month" :change-grouping :month] ["Year" :change-grouping :year]])

(defn create-buttons
  [btns]
  [:div.btn-group.bs-docs-sidenav.affix.btn-top-margin {:data-toggle "buttons-radio"}
   (for [[text mouse dt-type] btns]
     [:button.btn.btn-primary {:type :button :mouse mouse :dt-type dt-type :id text} text])])

(def spinner [:div.row-fluid
              [:div.span6.offset4
               [:img {:src "/img/ajax-loader.gif"}]]])

(defn heading
  [text]
  [:div.row-fluid
   [:div.span12.page-header.title-color
    [:h1 text]]])

(defn twitter-div
  [item]
  [:div (str "Tweeted on " (item :day))
   [:blockquote
    [:p (str (item :text))]
    [:small tw-uname]]])

(defn github-div
  [item]
  [:div
   [:code "Github Activity"]
   (str " on " (item :day))
   [:p
    [:span.muted (item :type)]
    [:span " in repo "]
    [:em (str " "  (item :repo))]]])

(defn dt-title
  [text]
  [:div.row-fluid
   [:div.span12
    [:h2.title-color text]]])

(defn render-all
  [{:keys [data tw-uname]}]
  [:div.container-fluid
   [:div.row-fluid
    [:div.span2
     (create-buttons dt-types)]
    [:div.span10
     (heading "Mashup Generator")
     (if (nil? data)
       spinner
       (for [[dt-group items] data]
         [:div
          (dt-title dt-group)
          (for [item items
                :let [source (item :source)]]
            [:div.row-fluid
             [:div.span12
              (case source
                :twitter (twitter-div item)
                :github (github-div item))]])]))]]
   [:script {:type "text/javascript"}
   "$(document).ready(function(){$('#Day').button('toggle');});"]])


