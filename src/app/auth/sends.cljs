(ns app.auth.sends
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [app.sends :refer [send github create-sender]]
            [github.core :as ghc]))

(defn json->user
  [{:keys [login url name location]}]
  [{:user/login    login
    :user/url      url
    :user/location location
    :user/name     name}
   {:user/current true
    :remote/status :success
    :user/user [:user/login login]}])

(defmethod send :user/current
  [key ast callback]
  (if-let [token (get-in ast [:params :login/token])]
    (let [request (ghc/GET-current-user-request github)
          sender (create-sender key [key true] token callback)]

      (sender request (fn [{:keys [json ok]}]
                (when ok
                  (json->user json)))))))