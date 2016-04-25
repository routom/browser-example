(ns app.auth.core
  (:require [routom.core :as r]
            [app.auth.reads]
            [app.auth.mutations]
            [app.auth.sends]
            [app.auth.ui.login :refer [Login]])
  (:import goog.module.ModuleManager))

(defmethod r/init-module "auth"
  [_]
  {:ui Login
   :bidi/path  "/login/"
   })

(defn init
  []
  (-> goog.module.ModuleManager .getInstance (.setLoaded "auth")))

(when-not js/goog.DEBUG
  (init))
