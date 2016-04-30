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
    {:value entities}))


(defmethod p/read :repo/by-name
  [{:keys [state query ast]} key {:keys [user/login repo/name]}]
  (let [db @state
        entities (d/q '[:find [(pull ?e ?selector) ...]
                        :in $ ?login ?name ?selector
                        :where [?e :repo/name ?name]
                        [?e :repo/owner ?owner]
                        [?owner :user/login ?login]]
                      db login name (or query '[*]))]
    {:value (first entities)}))

(defmethod p/read :tree/by-name
  [{:keys [state query] :as env} key {:keys [user/login repo/name branch] :as params}]
  (if (and login name branch)
    (let [db @state
          id [login name branch]
          entities (d/q '[:find [(pull ?e ?selector)]
                          :in $ ?id ?selector
                          :where
                          [?e :tree/id ?id]]
                        db id (or query '[*]))]
      (if-let [e (first entities)]
        {:value e}
        (p/remote-with-token env params)))))

(defmethod p/read :content/by-path
  [{:keys [state query] :as env} key {:keys [user/login repo/name branch path] :as params}]
  (if (and login name branch path)
    (let [db @state
          id [login name branch]
          entities (d/q '[:find [(pull ?e ?selector)]
                          :in $ ?id ?path ?selector
                          :where
                          [?b :branch/id ?id]
                          [?b :branch/tree ?t]
                          [?t :tree/tree ?e]
                          [?e :tree-item/content]
                          [?e :tree-item/path ?path]]
                        db id path (or query '[*]))]
      (if-let [e (first entities)]
        {:value e}))))

(defmethod p/read :remote.blob
  [{:keys [state query] :as env} key {:keys [user/login repo/name branch path] :as params}]
  (p/read-remote-with-token env [key [login name branch path]] params))

(defmethod p/read :repos/list
  [env key {:keys [user/login] :as params}]
  (p/read-remote-with-token env [key login] params))

(defmethod p/read :repo/detail
  [env key {:keys [user/login repo/name] :as params}]
  (p/read-remote-with-token env [key [login name]] params))

(defmethod p/read :repos
  [env key _]
  {:value {:a 2}})

(defmethod p/read :route.repos/list
  [env key _]
  {:value {:a 2}})

(defmethod p/read :route.repo/detail
  [env key _]
  {:value {:a 2}})

(defmethod p/read :route.repo/tree-item
  [env key _]
  {:value {:a 2}})

(defmethod p/read :route.repo/branch
  [env key _]
  {:value {:a 2}})
