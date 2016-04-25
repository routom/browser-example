(ns app.auth.sends
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [app.sends :refer [send]]
            [fetch.core :as f]
            [cljs.core.async :as async]
            [github.users :as u]))

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
    (let [
          token-header (str "token " token)
          request (assoc-in u/current-user-request [:headers "Authorization"] token-header)]
      (callback {key {key true
                      :remote/status :loading}})
      (let [[success error timeout :as channels] (f/fetch-json request)]
        (go
          (let [[value ch] (async/alts! channels)]
            (condp = ch
              success (let [{:keys [json ok]} value]
                        (when ok
                          (callback {key (json->user json)})))
              error (callback {key {:user/current true
                                    :remote/status :error
                                    :remote/error value}})
              timeout (callback {key {:user/current true
                                      :remote/status :timeout}})))))))
  )