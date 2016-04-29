(ns app.repos.core
  (:require [routom.core :as r]
            [app.repos.reads]
            [app.repos.sends]
            [app.repos.ui.list :refer [RepoList]]
            [app.repos.ui.detail :refer [Repo Branch]]
            [app.repos.ui.tree-item :refer [Blob]]
            )
  (:import goog.module.ModuleManager))

(defmethod r/init-module "repos"
  [_]
  {:bidi/path ["/users/" :user/login]
   :sub-routes
              {:route.repos/list
               {:ui        RepoList
                :bidi/path "/repos"}
               :route.repo/detail
               {:ui        Repo
                :bidi/path ["/repos/" :repo/name]
                :sub-routes
                           {:route.repo/branch
                            {
                             :ui        Branch
                             :bidi/path ["/" :branch]
                             :sub-routes
                             {:route.repo/tree-item
                              {:ui Blob
                               :bidi/path ["/" :path]}}}}}}
   })

(defn init
  []
  (-> goog.module.ModuleManager .getInstance (.setLoaded "repos")))

(when-not js/goog.DEBUG
  (init))
