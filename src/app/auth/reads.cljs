(ns app.auth.reads
  (:require [app.parser :as p]))

(defmethod p/read :user/current
  [{:keys [state query] :as env} key params]
  (p/remote-forced-read
    true env key params
    #(p/pull-one-by-attr-value @state key true query)))