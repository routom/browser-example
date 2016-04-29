(ns github.repos
  (:require [github.core :as gh]
            [github.users :as ghu]))

(defprotocol IRepo
  (get-repo [this])
  (get-branches [this])
  (get-collaborators [this])
  (get-contents [this branch path])
  (get-tree [this branch-or-sha]))


(deftype Repo [github user repo-name]
  IRepo
  (get-contents [_ branch path]
    (gh/GET-json-request github (str "/repos/" (ghu/get-username user) "/" repo-name "/contents/" path "?ref=" branch)))
  (get-repo [_]
    (gh/GET-json-request github (str "/repos/" (ghu/get-username user) "/" repo-name)))
  (get-tree [_ branch-or-sha]
    (gh/GET-json-request github (str "/repos/" (ghu/get-username user) "/" repo-name "/git/trees/" branch-or-sha)))
  (get-branches [_]
    (gh/GET-json-request github (str "/repos/" (ghu/get-username user) "/" repo-name "/branches")))
  (get-collaborators [_]
    (gh/GET-json-request github (str "/repos/" (ghu/get-username user) "/" repo-name "/collaborators"))))



