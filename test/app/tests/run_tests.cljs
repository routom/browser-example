(ns ^:figwheel-no-load app.tests.run-tests
  (:require [cljs.test :refer-macros [run-tests]]
            [github.tests.repos]))

(enable-console-print!)

(defn main []
  (run-tests 'github.tests.repos))

(defmethod cljs.test/report [:cljs.test/default :end-run-tests] [m]
  (if (cljs.test/successful? m)
    (println "Success!")
    (println "FAIL")))

(defn on-js-reload []
  (.log js/console "reloading tests")
  (main))

(main)