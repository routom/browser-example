(ns app.core
  (:require [goog.dom :as gdom]
            [app.core.shell :refer [Shell]]
            [routom.core :as r]
            [routom.bidi :as rb]
            [app.state :as s]
            [app.sends :as sends]
            [app.core.core]
            [app.core.mutations]
            [om.dom :as dom]
            [datascript.core :as d]
            [om.next :as om :refer-macros [defui]]
            [app.parser :as p]))


(enable-console-print!)

(defmethod p/read :home
  [env key params]
  {:value {:home/content "Hi"}})

(defui Home
  static om/IQuery
  (query [this] [:home/content])
  Object
  (render [this]
    (dom/div nil
             (dom/div nil "Home page")
             (dom/a #js {:href (-> (om/shared this :history)
                                   (.createHref "/login/"))}
                    "Login"))))

(def rest-expr [#".+" :rest])

(def routes
  (atom
    {:home {:ui Home :bidi/path "/"}
     :login {:module-id "auth" :bidi/path ["/login" rest-expr]}

     :shell {:ui Shell
             :sub-routes
                 {:repos {:module-id "repos" :bidi/path ["/users" rest-expr]}}}}))

(defn send
  [{:keys [remote] :as remotes} callback]
  (let [ast (om/query->ast remote)]
    (doseq [child-ast (:children ast)]
      (let [dispatch-key (:dispatch-key child-ast)]
        (sends/send dispatch-key child-ast #(callback % remote))))))


(defn merge-novelty!
  [reconciler db res query]
  (let [conn (if (d/conn? db) db (d/conn-from-db db))]
    (d/transact! conn (mapcat #(cond
                                (sequential? %) %
                                (map? %) [%]) (vals res)))
    @conn))

(defn merge-datascript [reconciler state res query]
  {:keys    (into [] (remove symbol?) (keys res))
   :next    (merge-novelty! reconciler state res query)
   :tempids (->> (filter (comp symbol? first) res)
                 (map (comp :tempids second))
                 (reduce merge {}))})

(defn init
  []
  (let [{:keys [root-class set-route! get-route ui->props]} (r/init-router routes #(dom/div nil (str "loading module " %2 " status: " %1)))
        useQueries js/window.History.useQueries
        createHistory (useQueries js/window.History.createHashHistory)
        history (createHistory)
        bidi-router (rb/start-bidi-router!
                      history
                      set-route!
                      routes
                      {:route/id :home :route/params {:rest "/"}})
        reconciler (om/reconciler
                     {:parser p/parser
                      :ui->props ui->props
                      :state  s/conn
                      :normalize false
                      :send send
                      :merge merge-datascript
                      :migrate nil
                      :shared {:bidi-router bidi-router
                               :history history}})]

    (let [root (om/add-root! reconciler root-class (gdom/getElement "app"))]
      {:reconciler reconciler
       :root root
       :history history
       :get-route get-route
       :set-route! set-route!
       :root-class root-class
       :routes routes
       :bidi-router bidi-router})))