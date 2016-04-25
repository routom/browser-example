(ns github.tests.repos
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :as a :refer [chan]]
    [cljs.test :refer-macros [deftest async is testing run-tests]]
    [app.repos.sends :as s]
    [app.sends :refer [send]]
    ))

(goog-define TOKEN "")

(deftest repos-by-user
  (async done
    (let [ch (chan)]
      (send :repos/list {:params {:user/login "octocat"
                                  :login/token TOKEN
                                  :repos.list/page 1
                                  :repos.list/per-page 10}}
            #(a/put! ch %))
      (go
        (is (= :loading (get-in (a/<! ch) [:repos/list :remote/status])))
        (let [repos (get (a/<! ch) :repos/list)]
          (println repos)
          (is (= (count repos) 12)))
        (is (= :success (get-in (a/<! ch) [:repos/list :remote/status])))
        (done)))))
