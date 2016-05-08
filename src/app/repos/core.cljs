(ns app.repos.core
  (:require [routom.core :as r]
            [app.repos.reads]
            [app.repos.mutations]
            [app.repos.sends]
            [app.repos.ui.list :refer [RepoList]]
            [app.repos.ui.detail :refer [Repo Branch]]
            [app.repos.ui.tree-item :refer [Blob]]
            ))

(defmethod r/init-module "repos"
  [_]
  {:sub-routes
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

