(ns app.state
  (:require [datascript.core :as d]
            [app.auth.schema :as auth]
            [app.repos.schema :as repos]))

(def schema (merge-with merge
              auth/schema
              repos/schema))

(def conn (d/create-conn schema))

