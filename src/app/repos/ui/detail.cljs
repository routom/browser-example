(ns app.repos.ui.detail
  (:require [om.next :as om :refer-macros [defui]]
            [app.components :as dom]
            [routom.core :as r]))

(defui EditableRepoDescription
  Object
  (initLocalState [this]
    (let [{:keys [repo/description repo/homepage]} (:repo (om/props this))]
      {:editing     false
       :description description
       :homepage    homepage}))
  (render [this]
    (let [{:keys [homepage description editing] :as state} (om/get-state this)]

      (if editing
        (dom/div #js {:style #js {:marginBottom 10}}
                 (dom/div nil
                          (dom/label #js {:style #js {:display "block"}} "Description")
                          (dom/input #js {:style #js {:width 400}
                                          :value    (or description "")
                                          :onChangeText #(om/update-state! this assoc :description %)}))
                 (dom/div nil
                          (dom/label #js {:style #js {:display "block"}} "Website")
                          (dom/input #js {:value    (or homepage "")
                                          :onChange #(om/update-state! this assoc :homepage %)}))

                 (dom/button {:onPress (fn [_]
                                              (om/update-state! this update :editing not)
                                              ((om/get-computed this :update) description homepage))}
                             (dom/text nil "Save"))
                 (dom/button {:onPres (fn [_]
                                        (let [{:keys [repo/description repo/homepage]} (:repo (om/props this))]
                                          (om/update-state! this #(-> %
                                                                      (update :editing not)
                                                                      (assoc :description description)
                                                                      (assoc :homepage homepage)))))}
                             (dom/text nil "Cancel")))
        (dom/div nil
                 (dom/p nil (dom/span nil description)
                        (dom/span nil " ")
                        (dom/span nil
                                  (dom/text #js {:onPress #(let [set-route! (om/shared this :set-route!)]
                                                            (set-route! {:route/id :route.repos/list
                                                                         :route/params (om/get-computed this :route/params)}))} homepage))
                        (dom/text #js {:onPress #(om/update-state! this update :editing not)}
                               (dom/text nil " -- EDIT"))))))))

(def repo-description (om/factory EditableRepoDescription))

(defui Repo
  static om/Ident
  (ident [this {:keys [db/id]}]
    [:db/id id])
  static om/IQuery
  (query [this] '[*])
  static r/IRootQuery
  (root-query [this]
    '[({:repo/by-name
        [:db/id
         :repo/default-branch
         :repo/name
         :repo/description
         :repo/homepage
         {:repo/owner [:user/login]}
         {:repo/branches [:branch/name
                          :branch/id]}]}
        {:user/login ?user/login :repo/name ?repo/name})
      ])
  static om/IQueryParams
  (params [this]
    {:user/login ""
     :repo/name ""
     :branch nil})
  Object
  (render [this]
    (let [[repo-remote repo] (om/get-computed this :repo/by-name)
          [tree-remote tree] (om/get-computed this :tree/by-name)
          branches (get-in repo [:repo/branches])
          {:keys [repo/name repo/default-branch]} repo
          router (om/shared this :bidi-router)
          history (om/shared this :history)
          {:keys [user/login branch] :as route-params} (om/get-computed this :route/params)
          route-id (om/get-computed this :route/id)]

      (if (and branches (not tree) default-branch)
        (let [set-route! (om/shared this :set-route!)]
          (set-route! {:route/id :route.repo/branch :route/params (assoc route-params :branch default-branch)})))

      (dom/div nil
               (dom/a #js {:onPress (fn []
                                      (let [set-route! (om/shared this :set-route!)]
                                        (set-route! {:route/id :route.repos/list
                                                     :route/params {:user/login login}})))}
                      (dom/text nil login))
               (dom/text (str " / " name))

               (if repo
                 (repo-description
                   (om/computed
                     {:repo repo}
                     {:update #(om/transact! this `[(repo/update {:repo ~(-> repo
                                                                            (assoc :repo/description %1)
                                                                            (assoc :repo/homepage %2))})])})))
               (when branches

                 (dom/div nil
                          (dom/select
                            #js {:selectedValue (or branch "")
                                 :onValueChange #(let [set-route! (om/shared this :set-route!)]
                                                  (set-route! {:route/id route-id :route/params (assoc route-params :branch %)})
                                                  )}
                            (clj->js (mapv #(dom/option
                                             #js {:label (:branch/name %)
                                                  :value (:branch/name %)
                                                  :key   (:branch/name %)}
                                             (:branch/name %)) branches)))
                          (r/try-render-subroute this)))))))

(defui Branch
  static r/IRootQuery
  (root-query [this]
    '[({:tree/by-name
        [:tree/sha
         :tree/url
         :tree/readme
         :tree/truncated
         {:tree/tree
          [:tree-item/path
           :tree-item/mode
           :tree-item/type
           :tree-item/sha
           :tree-item/size
           :tree-item/url]}]}
        {:user/login ?user/login
         :repo/name  ?repo/name
         :branch     ?branch})])
  static om/IQuery
  (query [this] '[*])
  static om/IQueryParams
  (params [this]
    {:branch nil})
  Object
  (render [this]
    (let [[tree-remote tree] (om/get-computed this :tree/by-name)
          route-params (om/get-computed this :route/params)
          router (om/shared this :bidi-router)]
      (if (r/has-subroute this)
        (r/render-subroute this)

        (if tree
          (let [by-type (group-by #(let [type (:tree-item/type %)]
                                    (condp = type
                                      "blob" "blob"
                                      "file" "blob"
                                      "tree" "tree")) (sort-by :tree-item/path (:tree/tree tree)))]
            (println tree)
            (dom/div nil
                     (map #(dom/div
                            #js {:key (:tree-item/path %)}
                            (dom/div nil (dom/text nil (:tree-item/path %)))
                            (dom/div nil (dom/text nil "tree"))) (get by-type "tree"))
                     (map #(dom/div
                            #js {:key (:tree-item/path %)}
                            (dom/div
                              nil
                              (dom/a
                                #js {:onPress (fn []
                                                (let [set-route! (om/shared this :set-route!)]
                                                  (set-route! {:route/id     :route.repo/tree-item
                                                               :route/params (assoc route-params :path (:tree-item/path %))})))}
                                (dom/text nil (:tree-item/path %))))
                            (dom/div nil (dom/text nil "file"))) (get by-type "blob")))))))))