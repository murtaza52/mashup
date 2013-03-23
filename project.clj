
(defproject mashup "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [compojure "1.1.5" :exclusions [[commons-io] [ring/ring-core] org.clojure/tools.macro]]
                 [clj-oauth "1.4.0"]
                 [org.apache.httpcomponents/httpclient "4.2.3"]
                 [enlive "1.0.1" :exclusions [org.clojure/clojure]]
                 [org.thnetos/cd-client "0.3.4" :exclusions [[org.clojure/clojure] clj-http cheshire commons-codec]]
                 [ring/ring-core "1.2.0-beta1"]
                 [twitter-api "0.7.2" :exclusions [[org.apache.httpcomponents/httpclient] [org.apache.httpcomponents/httpmime]]]
                 [tentacles "0.2.4" :exclusions [[cheshire] clj-http]]
                 [core.logic "0.6.1-SNAPSHOT"]
                 [webfui "0.2.1"]
                 [prismatic/dommy "0.0.2"]
                 [clj-time "0.4.4"]
                 [me.shenfeng/mustache "1.1"]
                 [shoreleave "0.3.0"]
                 [shoreleave/shoreleave-remote-ring "0.3.0"]
                 [shoreleave/shoreleave-remote "0.3.0"]
                 [domina "1.0.1"]
                 [org.clojure/google-closure-library-third-party "0.0-2029"]]
  :plugins [[lein-ring "0.8.3"]
            [lein-cljsbuild "0.3.0"]
            [lein-midje "3.0.0"]
            [lein-marginalia "0.7.1"]
            [lein-pallet-fuz "0.1.1"]
            [org.cloudhoist/pallet-lein "0.5.1"]]
  :ring {:handler mashup.handler/app}
  :repl-options {:init-ns mashup.handler
                 :init (do
                         (use 'ring.util.serve)
                         (serve app)
                         (use 'ring.mock.request))
                 }
  :profiles {:dev {:dependencies [[ring-mock "0.1.3"]
                                  [ring-serve "0.1.2"]
                                  [midje "1.5.0" :exclusions [joda-time]]
                                  [marginalia "0.7.1" :exclusions [org.clojure/tools.namespace]]]}}
  :cljsbuild {
              :repl-listen-port 9000
              :builds [{
                                        ; The path to the top-level ClojureScript source directory:
                        :source-paths ["src-cljs"]
                                        ; The standard ClojureScript compiler options:
                                        ; (See the ClojureScript compiler documentation for details.)
                        :compiler {
                                   :output-to "resources/public/js/cljs.js"  ; default: target/cljsbuild-main.js
                                   :optimizations :whitespace
                                   :pretty-print true}}]})
