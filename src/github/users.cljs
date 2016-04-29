(ns github.users
  (:require [github.core :as c]))

(defprotocol IUser
  (get-username [x])
  (get-repositories [x page per-page]))

(defrecord User [github username]
  IUser
  (get-username [_] username)
  (get-repositories [_ page per-page]
    (let [path-and-query (str "/users/" username "/repos?page=" page "&per_page=" per-page)]
      (c/GET-json-request github path-and-query))
    ))

(def current-user-request
  {:url "https://api.github.com/user"
   :method "GET"
   :headers {"Accept" "application/json"}}
  )