(ns app.core.shell
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(defui Shell
  static om/IQuery
  (query [_] [:shell/title])
  Object
  (render [this]
    (let [{:keys [shell/title]} (om/props this)]
      (dom/div
        nil
        (dom/div #js {:key "title"} title)
        (om/children this)))))