(ns app.dev
  (:require
    [app.auth.core]
    [app.repos.core]
    [app.core]
    [om.next :as om]
    [datascript.core :as d]
    [cljs.pprint :refer [pprint]]))


(def app (app.core/init))

(def reconciler (:reconciler app))

(def app-state (om/app-state reconciler))

(defn query
  [attr selector]
  (pprint (d/q
            '[:find [(pull ?e ?selector) ...]
              :in $ ?attr ?selector
              :where [?e ?attr]]
            @app-state attr selector)))

(defn transact!
  [tx-data]
  (d/transact! app-state tx-data))

(defn set-access-token!
  [token]
  (transact! [{:login true
               :login/token token}]))

(defn query*
  [q & args]
  (pprint (apply d/q
            q
            @app-state args)))

(defn on-js-reload
  []
  (app.core/init))