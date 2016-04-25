(ns app.auth.ui.login
  (:require [om.next :as om :refer-macros [defui]]
            [routom.core :as r]
            [routom.bidi :as rb]
            [om.dom :as dom])
  (:import [goog.net cookies]))


(defui Login
  static r/IRootQuery
  (root-query [_] [{:user/current
                    [:remote/status :remote/error
                     {:user/user [:user/login :user/url]}]}])
  static om/IQuery
  (query [this]
    [:login/token])
  Object
  (render [this]
    (let [{token :login/token :as props} (om/props this)
          {:keys [user/user remote/status]}
            (om/get-computed this [:user/current])]
      (println "login props: " props)
      (dom/div
        nil
        (condp = status
          :success (.render-success this token user)
          :error (dom/div nil "An error occurred")
          :timeout (dom/div nil "The request timed out")
          :loading (.render-initial this token status)
          (.render-initial this token status))
        )))
  (render-success
    [this token {:keys [user/login user/url] :as user}]
    (if user
      (let [_ (.set cookies "GITHUB_TOKEN" token)]
        (dom/a
          #js {:href (str "#/users/" login "/repos")}
          (str "hello " login)))))
  (render-initial
    [this token status]
    (dom/div
      nil
      (dom/div
        nil
        (dom/label #js {} "GitHub Personal Access Token"))
      (dom/div
        nil
        (dom/input
          #js {:type     "password"
               :onChange #(om/transact!
                           this
                           `[(login/update-token ~{:login/token (.. % -target -value)})])
               :value    token})
        (dom/button
          #js {:disabled (= :loading status)
               :onClick
               #(om/transact!
                 this
                 `[(login/update-token ~{:login/token token}) ~(om/force :user/current)])
                         } "Submit")))))