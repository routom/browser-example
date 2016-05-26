(ns app.repos.ui.tree-item
  (:require [om.next :as om :refer-macros [defui]]
            [app.components :as dom]
            [routom.core :as r]))

(defui Blob
  static r/IRootQuery
  (root-query [this]
    '[({:tree-item/by-path [:db/id
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
             (let [[remote blob] (om/get-computed this :tree-item/by-path)]
               (condp = (:remote/status remote)
                 :loading (dom/text nil "loading...")
                 :timeout (dom/text nil "the request timed out")
                 :error (dom/text nil "an error occurred while processing this request")
                 :success (let [{:keys [http/status http/ok]} remote]
                            (if ok
                              (dom/pre nil (:tree-item/content blob))
                              (dom/p nil (str "There was an error while retrieving the file contents. HTTP " status)))))))))
