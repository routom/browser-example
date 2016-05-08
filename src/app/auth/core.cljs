(ns app.auth.core
  (:require [routom.core :as r]
            [app.auth.reads]
            [app.auth.mutations]
            [app.auth.sends]
            [app.auth.ui.login :refer [Login]]))

(defmethod r/init-module "auth"
  [_]
  {:ui Login
   :bidi/path  "/login/"
   })
