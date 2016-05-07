(ns app.repos.mutations
  (:require [om.next :as om]
            [datascript.core :as d]
            [app.parser :as p]))

(defmethod p/mutate 'repo/update
  [{:keys [state] :as env} key {:keys [repo] :as params}]
  {:remote (p/ast-with-token env)
   :action (fn []
             (d/transact! state [repo]))})