(defproject mashup "0.1.0"
  :description "Mashup Generator"
  :url "http://github.com/murtaza52/mashup"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [compojure "1.1.5" :exclusions [[commons-io] [ring/ring-core] org.clojure/tools.macro]]
                 [clj-oauth "1.4.0"]
                 [org.apache.httpcomponents/httpclient "4.2.3"]
                 [org.thnetos/cd-client "0.3.4" :exclusions [[org.clojure/clojure] clj-http cheshire commons-codec]]
                 [ring/ring-core "1.2.0-beta1"]
                 [twitter-api "0.7.2" :exclusions [[org.apache.httpcomponents/httpclient] [org.apache.httpcomponents/httpmime]]]
                 [tentacles "0.2.4" :exclusions [[cheshire] clj-http]]
                 [webfui "0.2.1"]
                 [clj-time "0.4.4"]
                 [shoreleave "0.3.0"]
                 [shoreleave/shoreleave-remote-ring "0.3.0"]
                 [shoreleave/shoreleave-remote "0.3.0"]
                 [domina "1.0.1"]
                 [org.clojure/google-closure-library-third-party "0.0-2029"]]
  :plugins [[lein-ring "0.8.3"]
            [lein-cljsbuild "0.3.0"]
            [lein-midje "3.0.0"]
            [lein-marginalia "0.7.1"]]
  :ring {:handler mashup.handler/app}
  :repl-options {:init-ns mashup.handler
                 :init (do
                         (use 'ring.util.serve)
                         (serve app))}		;; serves the app when the repl starts.
  :profiles {:dev {:dependencies [[ring-mock "0.1.3"]
                                  [ring-serve "0.1.2"]
                                  [midje "1.5.0" :exclusions [joda-time]]
                                  [marginalia "0.7.1" :exclusions [org.clojure/tools.namespace]]]}}
  :cljsbuild {
              :builds
              [{:source-paths ["src-cljs"],
                :compiler
                {:pretty-print true,
                 :output-to "resources/public/js/cljs.js",
                 :optimizations :whitespace}}],
              :repl-listen-port 9000})
