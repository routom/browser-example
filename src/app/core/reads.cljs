(ns app.core.reads
  (:require [app.parser :as p]))

(defmethod p/read :shell
  [env key params]
  {:value (p/pull-one-by-attr-value @(:state env) key true (:query env))})