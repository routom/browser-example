(ns app.core.shell
  (:require [om.next :as om :refer-macros [defui]]
            [app.components :as c]
            [routom.core :as r]))

(defui Shell
  static r/IRootQuery
  (root-query [_] '[{:login [:login/token]}])
  static om/IQuery
  (query [_] [:shell/title
              {:shell/nav-state [:db/id
                                 :nav-state/key
                                 :nav-state/index
                                 {:nav-state/children [:db/id
                                                       :nav-state/key
                                                       :nav-state/params]}]}])
  Object

  (render [this]
    (println "shell props" (om/props this))
    (let [{:keys [shell/title shell/nav-state]} (om/props this)
          token (om/get-computed this [:login :login/token])
          on-navigate #(om/transact! this `[(nav/navigate {:nav-state ~nav-state :action ~(js->clj % :keywordize-keys true)})])
          subroute (r/render-subroute this {:on-navigate on-navigate})]
      (when-not token
        (om/transact! this '[(go/login)]))
      (c/navigation-animated-view
        #js {:navigationState (clj->js nav-state)
             :style           #js {:flex 1}
             :onNavigate      on-navigate
             :renderOverlay
                              (fn [props]
                                (c/navigation-header
                                  (clj->js
                                    (merge (js->clj props)
                                           {:renderTitleComponent
                                            (fn [props]
                                              (c/navigation-header-title
                                                nil "routom"))}))))
             :renderScene
                              (fn [props]
                                (c/navigation-card
                                  (clj->js (merge (js->clj props)
                                                  {:key (str "card_" (.. props -scene -navigationState -key))
                                                   :renderScene
                                                        (fn [props]
                                                          (c/scroll-view
                                                            #js {:style #js {:marginTop (.-HEIGHT c/NavigationHeader)}}
                                                            subroute))}))))

             })
      )))