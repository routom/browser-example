(defproject org.routom/browser-example "0.1.0-SNAPSHOT"
  :description ""
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.8.40"]
                 [org.clojure/core.async "0.2.374"]
                 [datascript "0.15.0"]
                 [org.omcljs/om "1.0.0-alpha32"]
                 [org.routom/routom "0.1.0-alpha5"]
                 [figwheel-sidecar "0.5.0-SNAPSHOT" :scope "test"]]

  :plugins [[lein-figwheel "0.5.2"]
            [lein-cljsbuild "1.1.3" :exclusions [[org.clojure/clojure]]]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src"]

                ;; If no code is to be run, set :figwheel true for continued automagical reloading
                :figwheel {:on-jsload "app.dev/on-js-reload"}

                :compiler {:main app.dev
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/main.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true}}

               {:id "test"
                :source-paths ["src" "test"]

                ;; If no code is to be run, set :figwheel true for continued automagical reloading
                :figwheel {:on-jsload "app.tests.run-tests/on-js-reload"}

                :compiler {:main app.tests.run-tests
                           :asset-path "js/compiled/out"
                           :closure-defines {"github.tests.repos.TOKEN" ~(System/getenv "GITHUB_TOKEN")}
                           :output-to "resources/public/js/compiled/tests.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true}}
               ;; This next build is an compressed minified build for
               ;; production. You can build this with:
               ;; lein cljsbuild once min
               {:id "min"
                :source-paths ["src"]
                :compiler {:output-dir "resources/public/js/compiled"
                           :main app.prod
                           :optimizations :advanced
                           :closure-defines {"goog/DEBUG" false}
                           :pretty-print false
                           :modules
                           {:auth
                            {:output-to "resources/public/js/compiled/auth.js"
                             :entries #{"app.auth.core"}}
                            :repos
                            {:output-to "resources/public/js/compiled/repos.js"
                             :entries #{"app.repos.core"}}}}}]}

  :figwheel {;; :http-server-root "public" ;; default and assumes "resources"
             :server-port 3459 ;; default
             ;; :server-ip "127.0.0.1"

             :css-dirs ["resources/public/css"] ;; watch and update CSS

             ;; Start an nREPL server into the running figwheel process
             ;; :nrepl-port 7888

             ;; Server Ring Handler (optional)
             ;; if you want to embed a ring handler into the figwheel http-kit
             ;; server, this is for simple ring servers, if this
             ;; doesn't work for you just run your own server :)
             ;; :ring-handler hello_world.server/handler

             ;; To be able to open files in your editor from the heads up display
             ;; you will need to put a script on your path.
             ;; that script will have to take a file path and a line number
             ;; ie. in  ~/bin/myfile-opener
             ;; #! /bin/sh
             ;; emacsclient -n +$2 $1
             ;;
             ;; :open-file-command "myfile-opener"

             ;; if you want to disable the REPL
             ;; :repl false

             ;; to configure a different figwheel logfile path
             ;; :server-logfile "tmp/logs/figwheel-logfile.log"
             })
