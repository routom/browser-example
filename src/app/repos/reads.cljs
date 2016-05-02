(ns app.repos.reads
  (:require [app.parser :as p]
            [datascript.core :as d]))


(defmethod p/read :repos/by-login
  [{:keys [state query] :as env} key {:keys [user/login page per-page] :as params}]
  (let [id login]
    (p/remote-read id env key params
                   (fn []
                     (let [db @state
                           entities (d/q '[:find [(pull ?e ?selector) ...]
                                           :in $ ?login ?selector
                                           :where [?e :repo/id]
                                           [?e :repo/owner ?owner]
                                           [?owner :user/login ?login]]
                                         db login (or query '[*]))]
                       entities)))))


(defmethod p/read :repo/by-name
  [{:keys [state query ast] :as env} key {:keys [user/login repo/name] :as params}]
  (let [id [login name]]
    (p/remote-read
      id env key params
      (fn []
        (let [db @state
              entities (d/q '[:find [(pull ?e ?selector) ...]
                              :in $ ?login ?name ?selector
                              :where [?e :repo/name ?name]
                              [?e :repo/owner ?owner]
                              [?owner :user/login ?login]]
                            db login name (or query '[*]))]
          (first entities))))))

(defmethod p/read :tree/by-name
  [{:keys [state query] :as env} key {:keys [user/login repo/name branch] :as params}]
  (let [id [login name branch]]
    (p/remote-read
      id env key params
      (fn []
        (if (and login name branch)
          (let [db @state
                branch-id [login name branch]
                entities (d/q '[:find [(pull ?tree ?selector) ...]
                                :in $ ?id ?selector
                                :where
                                [?e :branch/id ?id]
                                [?e :branch/tree ?tree]]
                              db branch-id (or query '[*]))]
            (first entities)))))))

(defmethod p/read :remote.blob
  [{query :query
    conn :state
    :as env} key {:keys [user/login repo/name branch path] :as params}]
  (let [id [login name branch path]]
    (p/remote-read id env key params
                 (fn []
                   (let [db @conn
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
                     (first entities))))))


