(ns app.state
  (:require [datascript.core :as d]
            [app.auth.schema :as auth]
            [app.repos.schema :as repos]))

(def schema (merge-with
              merge
              {:remote/by-id {:db/unique :db.unique/identity}}
              auth/schema
              repos/schema))

(defonce conn (d/create-conn schema))

