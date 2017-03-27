(defproject vashet "0.1.0-SNAPSHOT"
  :description "A ClojureScript wrapper for Robin Frischmann's css-in-js library fela"
  :url "http://github.com/derHowie/vashet"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.9.0-alpha15"]
                 [org.clojure/clojurescript "1.9.229"]
                 [org.clojure/core.async "0.3.442"]
                 [doo "0.1.6-SNAPSHOT"]
                 [reagent "0.6.0"]]

  :plugins [[lein-figwheel "0.5.9"]
            [lein-cljsbuild "1.1.5" :exclusions [[org.clojure/clojure]]]
            [lein-doo "0.1.7"]]

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
                                           :provides ["js.fela-prefixer"]}]
                           :source-map-timestamp true
                           :preloads [devtools.preload]}}
               {:id "test"
                :source-paths ["src" "test"]
                :compiler {:output-to "target/resources/test.js"
                           :output-dir "target/test/"
                           :main vashet.test-runner
                           :optimizations :none
                           :foreign-libs [{:file "js/fela.js"
                                           :provides ["js.fela"]}
                                          {:file "js/fela-dom.js"
                                           :provides ["js.fela-dom"]}
                                          {:file "js/fela-plugin-prefixer.js"
                                           :provides ["js.fela-prefixer"]}]
                           :pretty-print true
                           :source-map false}}
               {:id "min"
                :source-paths ["src"]
                :compiler {:output-to "resources/public/js/compiled/vashet.js"
                           :main vashet.core
                           :optimizations :advanced
                           :pretty-print false
                           :externs ["externs/externs.js"]}}]}
  :doo {:build "test"}
  :figwheel {}
  :profiles {:dev {:dependencies [[binaryage/devtools "0.9.0"]
                                  [figwheel-sidecar "0.5.9"]
                                  [com.cemerick/piggieback "0.2.1"]]
                   :source-paths ["src" "dev"]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}})
