(ns app.auth.sends
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [app.sends :refer [send create-sender]]
            [github.users :as ghu]
            [datascript.core :as d]))

(defn json->user
  [{:keys [login url name location]}]
  [{:user/current true

    :user/user    {:db/id         (d/tempid nil)
                   :user/login    login
                   :user/url      url
                   :user/location location
                   :user/name     name}}])

(defmethod send :user/current
  [key ast callback]
  (if-let [token (get-in ast [:params :login/token])]
    (let [request (ghu/GET-current-user-request)
          sender (create-sender key [key true] token callback)]

      (sender request (fn [{:keys [json ok]}]
                (when ok
                  (json->user json)))))))