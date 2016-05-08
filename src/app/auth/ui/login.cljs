(ns app.auth.ui.login
  (:require [om.next :as om :refer-macros [defui]]
            [routom.core :as r]
            [routom.bidi :as rb]
            [app.components :as dom]
            [react-native.linking :refer [open-url]]))


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
          :error (dom/text nil "An error occurred")
          :timeout (dom/text nil "The request timed out")
          :loading (.render-initial this token status)
          (.render-initial this token status))
        )))
  (render-success
    [this token {:keys [user/login user/url] :as user}]
    (if user
      (let [set-route! (om/shared this :set-route!)
            redirect (om/get-computed this [:route/params :redirect])]
        (if redirect
          (set-route! redirect)
          (set-route! {:route/id :route.repos/list :route/params {:user/login login}}))
        (dom/a
          #js {:onPress #(set-route! {:route/id :repos :route/params {:user/login login}})}
          (dom/text nil (str "Welcome " login))))))
  (render-initial
    [this token status]
    (dom/div
      nil
      (dom/div
        nil
        (dom/label nil "Enter a GitHub Personal Access Token"))
      (dom/div
        nil
        (dom/input
          #js {:secureTextEntry true
               :onChangeText #(om/transact!
                           this
                           `[(login/update-token ~{:login/token %})])
               :value    (or token "")})
        (dom/button
          #js {:disabled (= :loading status)
               :onPress
               #(om/transact!
                 this
                 `[(login/update-token ~{:login/token token}) ~(om/force :user/current)])
                         } (dom/text nil "Submit")))
      (dom/p nil "If you don't have one, you can generate a personal access token in your "
             )
      (dom/a #js {:onPress #(open-url "https://github.com/settings/tokens")} (dom/text nil "GitHub personal settings")))))