(defproject org.routom/browser-example "0.1.0-SNAPSHOT"
  :description ""
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.8.51"]
                 [org.clojure/core.async "0.2.374"]
                 [datascript "0.15.0"]
                 [cljsjs/moment "2.10.6-4"]
                 [org.omcljs/om "1.0.0-alpha30" :exclusions [cljsjs/react cljsjs/react-dom]]
                 [org.routom/routom "0.1.0-alpha8-SNAPSHOT" :exclusions [org.omcljs/om]]
                 [cljsjs/react "15.0.2-0"]
                 [cljsjs/react-dom "15.0.2-0"]
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
                           :asset-path "js/compiled/dev/out"
                           :output-to "resources/public/js/compiled/dev/main.js"
                           :output-dir "resources/public/js/compiled/dev/out"
                           :source-map-timestamp true}}

               {:id "test"
                :source-paths ["src" "test"]

                ;; If no code is to be run, set :figwheel true for continued automagical reloading
                :figwheel {:on-jsload "app.tests.run-tests/on-js-reload"}

                :compiler {:main app.tests.run-tests
                           :asset-path "js/compiled/out"
                           :closure-defines {"github.tests.repos.TOKEN" ~(or (System/getenv "GITHUB_TOKEN") "")}
                           :output-to "resources/public/js/compiled/test/tests.js"
                           :output-dir "resources/public/js/compiled/test/out"
                           :source-map-timestamp true}}
               ;; This next build is an compressed minified build for
               ;; production. You can build this with:
               ;; lein cljsbuild once min
               {:id "min"
                :source-paths ["src"]

                :compiler {
                           :output-dir "resources/public/js/min"
                           :asset-path "js/compiled/min/out"
                           :main app.prod
                           :source-map true
                           :optimizations :advanced
                           :closure-defines {"goog.DEBUG" false}
                           :pretty-print false
                           :parallel-build true
                           :verbose true
                           :externs ["resources/public/js/externs/js-base64.ext.js"
                                     "resources/public/js/externs/fetch.ext.js"
                                     "resources/public/js/externs/history.ext.js"]
                           :modules
                           {:auth
                            {:output-to "resources/public/js/min/auth.js"
                             :entries #{"app.auth.core"
                                        "app.auth.reads"
                                        "app.auth.mutations"
                                        "app.auth.schema"
                                        "app.auth.sends"
                                        "app.auth.ui.login"
                                        }}
                            :repos
                            {:output-to "resources/public/js/min/repos.js"
                             :entries #{"app.repos.core"
                                        "app.repos.mutations"
                                        "app.repos.reads"
                                        "app.repos.schema"
                                        "app.repos.sends"
                                        "app.repos.ui.detail"
                                        "app.repos.ui.list"
                                        "app.repos.ui.tree-item"}}
                            :cljs-base
                            {:output-to "resources/public/js/min/main.js"}}}}]}

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
