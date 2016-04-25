(ns app.core.reads
  (:require [app.parser :as p]))

(defmethod p/read :shell
  [env key params]
  {:value {:shell/title "GitHub Explorer"}})