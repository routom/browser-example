(ns app.sends
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :as async]
            [fetch.core :as f]))

(defmulti send (fn [key ast callback] key))


(defn create-sender
  [key [k val :as ident] request token callback]
  (let [
        token-header (str "token " token)
        request (assoc-in request [:headers "Authorization"] token-header)]
    (fn [response->txData]
      (callback {key {k val
                      :remote/status :loading}})
      (let [[success error timeout :as channels] (f/fetch-json request)]
        (go
          (let [[value ch] (async/alts! channels)]
            (condp = ch
              success (do
                        (callback {key (response->txData value)})
                        (callback {key {k            val
                                        :remote/status :success
                                        :http/response value}}))
              error (callback {key {k            val
                                    :remote/status :error
                                    :remote/error  value}})
              timeout (callback {key {k            val
                                      :remote/status :timeout}})))))))
  )


