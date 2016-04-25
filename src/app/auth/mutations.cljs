(ns app.auth.mutations
  (:require [app.parser :as p]
            [datascript.core :as d]))

(defmethod p/mutate 'login/update-token
  [{conn :state :as env} key {:keys [login/token]}]
  {:action (fn []
             (println "transacting " token)
             (d/transact!
                    conn
                    [{:login true
                      :login/token token}]))}
  )

