(ns app.repos.sends
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [app.sends :refer [send create-sender assign-namespace]]
            [github.core :as gh]
            [cljs.core.async :as async]
            [github.users :as ghu]
            [github.repos :as ghr]
            [datascript.core :as d]))

(defn json->repo
  [{:keys [owner default_branch] :as repo}]
  (let [owner-login (:login owner)]
    [{:user/login owner-login}
     (-> (assign-namespace repo "repo")
         (merge
           {:repo/owner (condp = (:type owner)
                          "User" [:user/login owner-login]
                          "Organization" [:org/login owner-login])
            :repo/default-branch default_branch}))]))

(defmethod send :repos/by-login
  [key {:keys [params]} callback]
  (if-let [{:keys [login/token user/login repos.list/page repos.list/per-page]} params]
    (let [ident [key login]
          request (ghu/get-repositories login page per-page)
          sender (create-sender key ident token callback)]
      (sender request (fn [{:keys [json get-header status ok]}]
                       (when ok
                         (let [link-header (get-header "Link")
                               link-map (if link-header
                                          (gh/parse-links link-header)
                                          {})]
                           (into [{:repos/by-login      login
                                   :repos.list/links link-map}] (mapcat #(json->repo %) json)))))))))

(defn json->branch [id login repo-name {:keys [name commit]}]
  {:repo/_branches [:repo/id id]
   :branch/name name
   :branch/id [login repo-name name]
   :branch/commit (:sha commit)})

(defmethod send :repo/by-name
  [key {:keys [params]} callback]
  (if-let [{:keys [login/token user/login repo/name]} params]
    (let [ident [key [login name]]
          request (ghr/get-repo login name)
          sender (create-sender key ident token callback)]
      (sender request (fn [{:keys [json get-header status ok]}]
                       (when ok
                         (let [repo (json->repo json)
                               id (:id json)]
                           (sender (ghr/get-branches login name)
                                   (fn [{:keys [json ok]}]
                                     (when ok
                                       (mapv #(json->branch id login name %) json))))
                           repo)))))))



(defn json->tree-item
  [json]
  (merge
    (assign-namespace json "tree-item")
    {:db/id (d/tempid nil)}))

(defn json->tree
  [id json]
  (let [tree (assign-namespace json "tree")
        tempid (d/tempid nil)]
    [{:branch/id id
      :branch/tree
                 (merge tree
                        {:db/id        tempid
                         :tree/id      id
                         :tree/tree    (map json->tree-item (:tree json))})}]))

(defmethod send :tree/by-name
  [key {params :params} callback]
  (let [{:keys [login/token user/login repo/name branch]} params
        id [login name branch]
        ident [key id]
        request (ghr/get-tree login name branch)
        sender (create-sender key ident token callback)]
    (sender request
            (fn [{:keys [json ok]}]
              (when ok
                (json->tree id json))))))

(defn json->blob
  [branch-id json]
  [{:branch/id branch-id
    :branch/tree {:db/id (d/tempid nil)
                  :tree/id branch-id
                  :tree/tree [(-> (assign-namespace json "tree-item")
                                  (assoc :db/id (d/tempid nil))
                                  (update :tree-item/content js/Base64.fromBase64))]}}])

(defmethod send :tree-item/by-path
  [key {params :params} callback]
  (let [{:keys [login/token user/login repo/name path branch remote/id]} params
        ident [key id]
        branch-id [login name branch]
        request (ghr/get-contents login name branch path)
        sender (create-sender key ident token callback)]
    (sender request
            (fn [{:keys [json ok]}]
              (when ok
                (json->blob branch-id (dissoc json :_links)))))))

(defmethod send 'repo/update
  [key {params :params} callback]
  (let [{:keys [login/token repo]} params
        {:keys [repo/owner repo/name repo/homepage repo/description]} repo
        owner (:user/login owner)
        ident [:repo/update [owner name]]
        request (ghr/edit-repo owner name {:name name :homepage homepage :description description})
        sender (create-sender key ident token callback)]
    (sender request
            (fn [{:keys [json ok]}]
              (when ok
                (println "repo/update" json))))))
