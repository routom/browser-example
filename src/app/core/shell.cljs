(ns app.core.shell
  (:require [om.next :as om :refer-macros [defui]]
            [app.components :as dom]
            [routom.core :as r]))

(defui Shell
  static r/IRootQuery
  (root-query [_] '[{:login [:login/token]}])
  static om/IQuery
  (query [_] [:shell/title])
  Object
  (render [this]
    (println "shell props" (om/props this))
    (let [{:keys [shell/title]} (om/props this)
          token (om/get-computed this [:login :login/token])]
      (when-not token
        (om/transact! this '[(go/login)]))
      (dom/div
        nil
        (dom/text #js {:key "title"} title)
        (r/render-subroute this)))))