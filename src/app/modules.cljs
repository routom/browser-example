(ns app.modules
  (:require [goog.module.ModuleManager :as module-manager]
            [goog.module.ModuleLoader])
  (:import goog.module.ModuleManager))

(def modules
  #js {"auth" "/js/min/auth.js"
       "repos" "/js/min/repos.js"})

(def module-info
  #js {"auth" []
       "repos" []})

(def manager (module-manager/getInstance))

(def loader (goog.module.ModuleLoader.))

(when js/goog.DEBUG
  (.setDebugMode loader true))

(defn init
  []
  (doto manager
    (.setLoader loader)
    (.setAllModuleInfo module-info)
    (.setModuleUris modules)))