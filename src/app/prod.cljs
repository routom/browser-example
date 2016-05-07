(ns app.prod
  (:require [app.modules]
            [app.core]))

(app.modules/init)
(app.core/init)
