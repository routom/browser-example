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
  (root-query [this] '[({:repos/by-login
                         [:repo/name
                          :repo/description
                          :repo/id
                          :repo/pushed_at
                          {:repo/owner [:user/login]}
                          :db/id]}
                         {:user/login ?user/login
                          :page       ?repos/page
                          :per-page   ?repos/per-page})])

  Object
  (render [this]
    (let [props (om/props this)
          {[remote repos] :repos/by-login params :route/params :as computed} (om/get-computed this)]
      (dom/div
        nil
        (dom/div nil "Repos")
        (condp = (:remote/status remote)
          :loading (dom/div nil "loading...")
          :error (dom/div nil "An unexpected error occurred")
          :timeout (dom/div nil "The operation timed out. Check your network connection")
          :success (.render-success this remote repos params)
          ))))

  (render-success [this remote repos params]
    (dom/div
      nil
      (dom/button
        #js {:onClick #(om/transact! this `[(remote/force) ~(om/force :repos/by-login)])}
        "Refresh")
      (dom/ul
        nil
        (map
          #(.render-repo-list-item this %) (sort-by :repo/pushed_at #(compare %2 %1) repos)))

      (let [last (get-in remote [:repos.list/links :last])
            uri (goog.Uri. last)
            bidi-router (om/shared this :bidi-router)]
        (dom/div
          nil
          (str "Page " (:repos/page params) " of " (.getParameterValue uri "page")
               (rb/path-for bidi-router :repos params (select-keys params [:per-page])))))))
  (render-repo-list-item [this repo]
    (dom/li
      #js {:key (:repo/id repo)}
      (dom/h2 nil
              (dom/a
                #js {:href (rb/href-for
                             (om/shared this :bidi-router)
                             :route.repo/detail
                             {:user/login (get-in repo [:repo/owner :user/login])
                              :repo/name  (:repo/name repo)})} (:repo/name repo)))
      (dom/p nil (:repo/description repo))
      (dom/p #js {:style #js {:fontStyle "italic"}}
             (dom/span nil (str "Last modified " (-> (:repo/pushed_at repo)
                                                     (js/moment)
                                                     (.fromNow))))))))

