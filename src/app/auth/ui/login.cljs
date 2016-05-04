(ns app.auth.ui.login
  (:require [om.next :as om :refer-macros [defui]]
            [routom.core :as r]
            [routom.bidi :as rb]
            [om.dom :as dom])
  (:import [goog.net cookies]))


(defui Login
  static r/IRootQuery
  (root-query [_] [{:user/current
                    [{:user/user [:user/login :user/url]}]}])
  static om/IQuery
  (query [this]
    [:login/token])
  Object
  (render [this]
    (let [{token :login/token :as props} (om/props this)
          [{:keys [remote/status]} {:keys [user/user]}] (om/get-computed this [:user/current])]
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
      (let [_ (.set cookies "GITHUB_TOKEN" token)
            redirect (om/get-computed this [:route/params :redirect])
            history (om/shared this :history)]
        (if redirect
          (.push history redirect)
          (.push history (str "/users/" login "/repos")))
        (dom/a
          #js {:href (str "#/users/" login "/repos")}
          (str "hello " login)))))
  (render-initial
    [this token status]
    (dom/div
      nil
      (dom/div
        nil
        (dom/label #js {} "Enter a GitHub Personal Access Token"))
      (dom/div
        nil
        (dom/input
          #js {:type     "password"
               :onChange #(om/transact!
                           this
                           `[(login/update-token ~{:login/token (.. % -target -value)})])
               :value    (or token "")})
        (dom/button
          #js {:disabled (= :loading status)
               :onClick
               #(om/transact!
                 this
                 `[(login/update-token ~{:login/token token}) ~(om/force :user/current)])
                         } "Submit"))
      (dom/p nil "If you don't have one, you can generate a personal access token in your "
             (dom/a #js {:href "https://github.com/settings/tokens"} "GitHub personal settings")))))