(defproject vashet "0.1.0-SNAPSHOT"
    :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.9.0-alpha15"]
                 [org.clojure/clojurescript "1.9.229"]
                 [org.clojure/core.async "0.3.442"]
                 [reagent "0.6.0"]]

  :plugins [[lein-figwheel "0.5.9"]
            [lein-cljsbuild "1.1.5" :exclusions [[org.clojure/clojure]]]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src"]
                :figwheel {:on-jsload "vashet.core/on-js-reload"
                           :open-urls ["http://localhost:3449/index.html"]}
                :compiler {:main vashet.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/vashet.js"
                           :output-dir "resources/public/js/compiled/out"
                           :foreign-libs [{:file "js/fela.js"
                                           :provides ["js.fela"]}
                                          {:file "js/fela-dom.js"
                                           :provides ["js.fela-dom"]}
                                          {:file "js/fela-plugin-prefixer.js"
                                           :provides ["js.fela-prefixer"]}
                                          {:file "js/fela-font-renderer.js"
                                           :provides ["js.fela-font-renderer"]}]
                           :source-map-timestamp true
                           :preloads [devtools.preload]}}
               {:id "min"
                :source-paths ["src"]
                :compiler {:output-to "resources/public/js/compiled/vashet.js"
                           :main vashet.core
                           :optimizations :advanced
                           :pretty-print false}}]}

  :figwheel {}
  :profiles {:dev {:dependencies [[binaryage/devtools "0.9.0"]
                                  [figwheel-sidecar "0.5.9"]
                                  [com.cemerick/piggieback "0.2.1"]]
                   :source-paths ["src" "dev"]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}})
