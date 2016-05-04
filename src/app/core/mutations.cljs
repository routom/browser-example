(ns app.core.mutations
  (:require [app.parser :as p]
            [om.next :as om]
            [routom.bidi :as rb]))

(defmethod p/mutate 'go/login
  [{conn :state c :component :as env} key _]
  {:action
   (fn []
     (let [history (om/shared c :history)
           bidi-router (om/shared c :bidi-router)
           current-location (.getCurrentLocation history)
           current-path (.createPath history current-location)
           login-path (.createPath history #js {:pathname "/login/" :query #js {:redirect current-path}})
           ]
       (.push history login-path)))})