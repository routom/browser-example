(ns app.dev
  (:require
    [app.auth.core]
    [app.repos.core]
    [app.modules]
    [app.core]
    [om.next :as om]
    [datascript.core :as d]
    [cljs.pprint :refer [pprint]]))


(app.modules/init)
(app.auth.core/init)
(app.repos.core/init)
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

(defn query*
  [q & args]
  (pprint (apply d/q
            q
            @app-state args)))