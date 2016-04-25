(ns app.repos.sends
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [app.sends :refer [send create-sender]]
            [fetch.core :as f]
            [github.core :as gh]
            [cljs.core.async :as async]
            [github.repos :as r]))

(defn json->repo
  [{:keys [id owner name url]} login]
  (let [owner-login (:login owner)]
    [{:user/login owner-login}
     {:repo/id    id
      :repo/name  name
      :repo/owner (condp = (:type owner)
                    "User" [:user/login owner-login]
                    "Organization" [:org/login owner-login])
      :repo/url   url}]))

(defmethod send :repos/list
  [key {:keys [params]} callback]
  (if-let [{:keys [login/token user/login repos.list/page repos.list/per-page]} params]
    (let [ident [key login]
          request (r/user-repos-request login page per-page)
          sender (create-sender key ident request token callback)]
      (sender (fn [{:keys [json get-header status ok]}]
                (when ok
                  (let [link-header (get-header "Link")
                        link-map (if link-header
                                   (gh/parse-links link-header)
                                   {})]
                    (into [{:repos/list      login
                            :repos.list/links link-map}] (mapcat #(json->repo % login) json)))))))))

