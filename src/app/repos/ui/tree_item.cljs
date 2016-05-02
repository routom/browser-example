(ns app.repos.ui.tree-item
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [routom.core :as r]))

(defui Blob
  static r/IRootQuery
  (root-query [this]
    '[({:remote.blob [:db/id
                      :tree-item/url
                      :tree-item/content
                      :tree-item/encoding
                      :tree-item/size]}
        {:user/login ?user/login
         :repo/name  ?repo/name
         :branch     ?branch
         :path       ?path}
        )])
  static om/IQueryParams
  (params [this] {:path nil})
  static om/IQuery
  (query [this]
    '[*])
  Object
  (render [this]
    (dom/div nil (dom/h1 nil (om/get-computed this [:route/params :path]))
             (let [[remote blob] (om/get-computed this :remote.blob)]
               (condp = (:remote/status remote)
                 :loading (dom/div nil "loading...")
                 :timeout (dom/div nil "the request timed out")
                 :error ()
                 :success (let [{:keys [http/status http/ok]} remote]
                            (if ok
                              (dom/pre nil (:tree-item/content blob))
                              (dom/p nil (str "There was an error while retrieving the file contents. HTTP " status)))))))))
