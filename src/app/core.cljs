(ns app.core
  (:require [app.core.shell :refer [Shell]]
            [routom.core :as r]
            [routom.bidi :as rb]
            [app.state :as s]
            [app.sends :as sends]
            [app.core.core]
            [app.core.mutations]
            [datascript.core :as d]
            [om.next :as om :refer-macros [defui]]
            [app.components :as c]
            [app.auth.core]
            [app.repos.core]
            [app.parser :as p]
            [re-natal.support :as sup]))


(enable-console-print!)

(defmethod p/read :home
  [env key params]
  {:value {:home/content "Hi"}})

(defui Home
  static om/IQuery
  (query [this] [:home/content])
  Object
  (render [this]
    (c/view nil
             (c/text nil "Home page")

             (c/touchable-highlight
               #js {:onPress #(let [set-route! (om/shared this :set-route!)]
                               (set-route! {:route/id :login :route/params {}})) }
               (c/text nil "Login")))))

(def rest-expr [#".+" :rest])

(def routes
  (atom
    {:home {:ui Home}
     :login (r/init-module "auth")

     :shell {:ui Shell
             :sub-routes
                 {:repos (r/init-module "repos")}}}))

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
  (let [{:keys [root-class set-route! get-route ui->props]} (r/init-router routes #(c/text nil (str "loading module " %2 " status: " %1)))
        reconciler (om/reconciler
                     {:parser      p/parser
                      :ui->props   ui->props
                      :state       s/conn
                      :normalize   false
                      :send        send
                      :merge       merge-datascript
                      :migrate     nil
                      :shared      {:set-route! set-route!}
                      :root-render sup/root-render
                      :root-unmount sup/root-unmount})]
    (set-route! {:route/id :home :route/params {}})

    {:reconciler reconciler
     :get-route get-route
     :set-route! set-route!
     :root-class root-class
     :routes routes}))