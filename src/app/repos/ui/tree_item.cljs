(ns app.repos.ui.tree-item
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [routom.core :as r]))

(defui Blob
  static r/IRootQuery
  (root-query [this]
    '[{(:remote.blob
         {:user/login ?user/login
          :repo/name ?repo/name
          :branch ?branch
          :path ?path})
       [:remote/status
        :remote/error]}
      {(:content/by-path
        {:user/login ?user/login
         :repo/name ?repo/name
         :branch ?branch
         :path ?path})
       [:db/id
        :tree-item/url
        :tree-item/content
        :tree-item/encoding
        :tree-item/size]}])
  static om/IQueryParams
  (params [this] {:path nil})
  static om/IQuery
  (query [this]
    '[*])
  Object
  (render [this]
    (let [blob (om/get-computed this :content/by-path)]
      (dom/pre nil (:tree-item/content blob)))))
