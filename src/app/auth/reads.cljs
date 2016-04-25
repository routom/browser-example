(ns app.auth.reads
  (:require [app.parser :as p]
            [datascript.core :as d])
  (:import [goog.net cookies]))

(defmethod p/read :login
  [{:keys [state query]} key _]
  (let [entities (p/pull-by-attr-value @state key true query)
        value (first entities)]
    (if value
      {:value value}
      (if-let [cookie (.get cookies "GITHUB_TOKEN")]
        {:value {:login/token cookie}}))))

(defmethod p/read :user/current
  [env key params]
  (p/read-targeted-remote-with-token env [key true] params))