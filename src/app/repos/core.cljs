(ns app.repos.core
  (:require [routom.core :as r]
            [app.repos.reads]
            [app.repos.sends]
            [app.repos.ui.list :refer [RepoList]])
  (:import goog.module.ModuleManager))

(defmethod r/init-module "repos"
  [_]
  {:ui RepoList
   :bidi/path  ["/users/" :user/login "/repos"]
   })

(defn init
  []
  (-> goog.module.ModuleManager .getInstance (.setLoaded "repos")))

(when-not js/goog.DEBUG
  (init))
