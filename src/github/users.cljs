(ns github.users
  (:require [github.core :as c]
            [github.core :as ghc]))

(defn get-repositories [user-login page per-page]
  (let [path-and-query (str "/users/" user-login "/repos?page=" page "&per_page=" per-page)]
    (ghc/GET-json-request path-and-query)))

(defn GET-current-user-request []
  (ghc/GET-json-request "/user"))