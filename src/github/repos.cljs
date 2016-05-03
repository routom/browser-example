(ns github.repos
  (:require [github.core :as gh]))


(defn get-contents [user repo branch path]
  (gh/GET-json-request (str "/repos/" user "/" repo "/contents/" path "?ref=" branch)))

(defn get-repo [user repo]
  (gh/GET-json-request (str "/repos/" user "/" repo)))

(defn get-tree [user repo-name branch-or-sha]
  (gh/GET-json-request (str "/repos/" user "/" repo-name "/git/trees/" branch-or-sha)))

(defn get-branches [user repo-name]
  (gh/GET-json-request (str "/repos/" user "/" repo-name "/branches")))

(defn get-collaborators [user repo-name]
  (gh/GET-json-request (str "/repos/" user "/" repo-name "/collaborators")))




