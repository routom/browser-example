(ns app.dev
  (:require
    [app.auth.core]
    [app.repos.core]
    [app.modules]
    [app.core]))


(app.modules/init)
(app.auth.core/init)
(app.repos.core/init)
(def app (app.core/init))