(defproject singularity "0.0.1-alpha"
  :description "Singularity Chess game"
  :url "http://github.com/luketurner/singularity-chess"
  :license {:name "MIT License"}

  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.238"]
                 [org.clojure/core.async "0.4.474"]
                 [reagent "0.7.0"]
                 [org.clojure/core.match "0.3.0-alpha5"]
                 [garden "1.3.6"]
                 [prismatic/schema "0.4.4"]]

  :plugins [[lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]
            [lein-figwheel "0.5.16"]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/public/js" "target"]

  :cljsbuild {:builds [{:id           "dev"
                        :source-paths ["src"]

                        :figwheel     {:on-jsload "singularity.core/on-js-reload"}

                        :compiler     {:main                 singularity.core
                                       :asset-path           "js/out"
                                       :output-to            "resources/public/js/singularity.js"
                                       :output-dir           "resources/public/js/out"
                                       :source-map-timestamp true
                                       :closure-defines      {goog.DEBUG true}}}
                       {:id           "min"
                        :source-paths ["src"]
                        :compiler     {:output-to       "resources/public/js/singularity.js"
                                       :main            singularity.core
                                       :optimizations   :advanced
                                       :pretty-print    false
                                       :closure-defines {goog.DEBUG false}}}]}

  :figwheel {
             ;; :http-server-root "public" ;; default and assumes "resources" 
             ;; :server-port 3449 ;; default
             ;; :server-ip "127.0.0.1" 

             :css-dirs ["resources/public/css"]})             ;; watch and update CSS

             ;; Start an nREPL server into the running figwheel process
             ;; :nrepl-port 7888

             ;; :open-file-command "myfile-opener"

             ;; if you want to disable the REPL
             ;; :repl false

             ;; to configure a different figwheel logfile path
             ;; :server-logfile "tmp/logs/figwheel-logfile.log" 
