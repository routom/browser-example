(ns app.repos.reads
  (:require [app.parser :as p]
            [datascript.core :as d]))


(defmethod p/read :repos/by-login
  [{:keys [state query ast]} key {:keys [user/login page per-page]}]
  (let [db @state
        entities (d/q '[:find [(pull ?e ?selector) ...]
                        :in $ ?login ?selector
                        :where [?e :repo/id]
                                [?e :repo/owner ?owner]
                        [?owner :user/login ?login]]
                      db login (or query '[*]))]
    {:value entities})
  )

(defmethod p/read :repos/list
  [env key {:keys [user/login] :as params}]
  (p/read-remote-with-token env [key login] params))

(defmethod p/read :repos
  [env key _]
  {:value {:a 2}})

