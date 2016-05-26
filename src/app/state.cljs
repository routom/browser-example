(ns app.state
  (:require [datascript.core :as d]
            [app.auth.schema :as auth]
            [app.repos.schema :as repos]))

(def schema (merge-with
              merge
              {:remote/by-id {:db/unique :db.unique/identity}
               :nav-state/key {:db/unique :db.unique/identity}
               :nav-state/children {:db/valueType :db.type/ref
                                    :db/isComponent true
                                    :db/cardinality :db.cardinality/many}
               :shell {:db/unique :db.unique/identity}
               :shell/nav-state {:db/valueType :db.type/ref
                                 :db/isComponent true
                                 :db/cardinality :db.cardinality/one}}
              auth/schema
              repos/schema))

(defonce conn (d/create-conn schema))

(d/transact!
  conn
  [{:shell true
    :shell/title "Routom Example"
    :shell/nav-state
    {:db/id (d/tempid nil)
     :nav-state/key :shell-nav-stack
     :nav-state/index -1
     :nav-state/children []
     }}])

