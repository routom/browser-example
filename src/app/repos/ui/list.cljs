(ns app.repos.ui.list
  (:require
    [routom.core :as r]
    [routom.bidi :as rb]
    [om.next :as om :refer-macros [defui]]
    [om.dom :as dom]))

(defui RepoList
  static om/IQuery
  (query [this] '[*])
  static om/IQueryParams
  (params [this] {:user/login ""
                  :repos/page 1
                  :repos/per-page 20})
  static r/IRootQuery
  (root-query [this] '[{(:repos/by-login {:user/login ?user/login
                                          :page ?repos/page
                                          :per-page ?repos/per-page})
                        [:repo/name :repo/id {:repo/owner [:user/login]}]}
                       {(:repos/list {:user/login ?user/login
                                      :repos.list/page ?repos/page
                                      :repos.list/per-page ?repos/per-page}) [:remote/status :remote/error :repos.list/links]}])

  Object
  (render [this]
    (let [props (om/props this)
          {remote :repos/list repos :repos/by-login params :route/params :as computed} (om/get-computed this)]
      (println props)
      (println computed)
      (dom/div
        nil
        (dom/div nil "Repos")
        (dom/div nil (pr-str remote))
        (dom/ul nil (map #(dom/li nil (dom/a #js {:key  (:repo/id %)
                                                  :href (rb/href-for
                                                          (om/shared this :bidi-router)
                                                          :route.repo/detail
                                                          {:user/login (get-in % [:repo/owner :user/login])
                                                           :repo/name  (:repo/name %)})} (:repo/name %)))repos))
        (let [last (get-in remote [:repos.list/links :last])
              uri (goog.Uri. last)
              bidi-router (om/shared this :bidi-router)]
          (dom/div
            nil
            (str "Page " (:repos/page params) " of " (.getParameterValue uri "page") (rb/path-for bidi-router :repos params (select-keys params [:per-page])))))))))

