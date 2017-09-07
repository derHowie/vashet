(defproject vashet "0.0.4"
  :description "A ClojureScript wrapper for Robin Frischmann's css-in-js library fela"
  :url "http://github.com/derHowie/vashet"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.9.0-alpha16"]
                 [org.clojure/clojurescript "1.9.908"]
                 [org.clojure/core.async "0.3.442"]
                 [org.clojure/spec.alpha "0.1.123"]
                 [cljsjs/fela "4.3.2-0"]
                 [cljsjs/fela-dom "4.3.2-0"]
                 [cljsjs/fela-plugin-prefixer "4.3.2-0"]
                 [reagent "0.6.0"]]

  :plugins [[lein-cljsbuild "1.1.5" :exclusions [[org.clojure/clojure]]]
            [lein-doo "0.1.7"]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {:builds
              [{:id "test"
                :source-paths ["src" "test"]
                :compiler {:output-to "target/resources/test.js"
                           :output-dir "target/test/"
                           :main vashet.test-runner
                           :optimizations :none
                           :pretty-print true
                           :process-shim false}}
               {:id "min"
                :source-paths ["src" "browser"]
                :compiler {:main browser.core
                           :output-to "resources/public/js/compiled/vashet.min.js"
                           :pretty-print false
                           :optimizations :advanced}}]}
  :doo {:build "test"})
