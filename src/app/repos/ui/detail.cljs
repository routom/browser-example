(ns app.repos.ui.detail
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [routom.core :as r]
            [routom.bidi :as rb]))

(defui Repo
  static om/IQuery
  (query [this] '[*])
  static r/IRootQuery
  (root-query [this]
    '[{(:repo/by-name {:user/login ?user/login :repo/name ?repo/name})
       [:repo/default-branch
        :repo/name
        :repo/description
        {:repo/branches [:branch/name
                         :branch/id
                         {:branch/tree [:tree/sha
                                        :tree/url
                                        :tree/readme
                                        :tree/truncated
                                        {:tree/tree
                                         [:tree-item/path
                                          :tree-item/mode
                                          :tree-item/type
                                          :tree-item/sha
                                          :tree-item/size
                                          :tree-item/url]}]}]}]}
      {(:tree/by-name
         {:user/login      ?user/login
          :repo/name       ?repo/name
          :branch ?branch})
       [:remote/status :remote/error]}

      {(:repo/detail
         {:user/login ?user/login
          :repo/name ?repo/name}) [:remote/status :remote/error]}])
  static om/IQueryParams
  (params [this]
    {:user/login ""
     :repo/name ""
     :branch nil})
  Object
  (render [this]
    (let [repo (om/get-computed this :repo/by-name)
          tree (om/get-computed this :tree/by-name)
          branches (get-in repo [:repo/branches])
          {:keys [repo/name repo/description repo/default-branch]} repo
          router (om/shared this :bidi-router)
          history (om/shared this :history)
          {:keys [user/login branch] :as route-params} (om/get-computed this :route/params)
          route-id (om/get-computed this :route/id)]
      (if (and branches (not tree) default-branch)
        (let [path (rb/path-for router :route.repo/branch (assoc route-params :branch default-branch))]
          (.replace history path)))

      (dom/div nil
               (dom/h1 nil
                       (dom/a #js {:href (rb/href-for router :route.repos/list {:user/login login})}
                              login)
                       (str " / " name))
               (dom/p nil description)
               (when branches
                 (dom/div nil
                          (dom/select
                            #js {:value    branch
                                 :onChange #(let [path (rb/path-for router route-id (assoc route-params :branch (.. % -target -value)))]
                                             ; using setTimeout because a synchronous (.push history)
                                             ; doesn't appear to trigger a re-render
                                             ; should probably use om/transact! instead
                                             (js/setTimeout (fn [] (.push history path)) 100))}
                            (map #(dom/option
                                   #js {:value (:branch/name %)}
                                   (:branch/name %)) branches))
                          (r/try-render-subroute this)))))))

(defui Branch
  static om/IQuery
  (query [this] '[*])
  static om/IQueryParams
  (params [this]
    {:branch nil})
  Object
  (render [this]
    (let [repo (om/get-computed this :repo/by-name)
          branches (get-in repo [:repo/branches])
          router (om/shared this :bidi-router)
          route-params (om/get-computed this :route/params)
          selected-branch (first (filter #(= (:branch route-params) (:branch/name %)) branches))]
      (if (r/has-subroute this)
        (r/render-subroute this {:repo repo :branches branches :selected-branch selected-branch})

        (if-let [selected-branch selected-branch]
          (if-let [tree (:branch/tree selected-branch)]
            (let [by-type (group-by :tree-item/type (sort-by :tree-item/path (:tree/tree tree)))]
              (dom/table nil
                         (dom/tbody nil
                                    (map #(dom/tr
                                           #js {:key (:tree-item/path %)}
                                           (dom/td nil (:tree-item/path %))
                                           (dom/td nil "tree")) (get by-type "tree"))

                                    (map #(dom/tr
                                           #js {:key (:tree-item/path %)}
                                           (dom/td
                                             nil
                                             (dom/a
                                               #js {:href (rb/href-for router :route.repo/tree-item (assoc route-params :path (:tree-item/path %)))}
                                               (:tree-item/path %)))
                                           (dom/td nil "file")) (get by-type "blob")))))))))))