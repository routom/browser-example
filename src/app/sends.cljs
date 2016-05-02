(ns app.sends
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :as async]
            [fetch.core :as f]
            [github.core :as ghc]))

(defmulti send (fn [key ast callback] key))

(def github (ghc/->GitHub "https://api.github.com"))

(defn assign-namespace
  [m ns]
  (reduce-kv
    (fn [m k v]
      (if v
        (assoc m (keyword ns (name k)) v)
        m)) {} m))

(defn create-sender
  [key [k val :as ident] token callback]
  (fn [request response->txData]
    (let [token-header (str "token " token)
          val (into [k] (if (sequential? val) val [val]))
          k :remote/by-id
          request (assoc-in request [:headers "Authorization"] token-header)]

      (callback {key {k              val
                      :remote/status :loading}})
      (let [[success error timeout :as channels] (f/fetch-json request)]
        (go
          (let [[value ch] (async/alts! channels)]
            (condp = ch
              success (let [{:keys [json ok status response get-header]} value]
                        (callback {key (response->txData value)})
                        (callback {key {k              val
                                        :remote/status :success
                                        :http/status status
                                        :http/ok ok
                                        :http/json json}}))
              error (callback {key {k              val
                                    :remote/status :error
                                    :remote/error  value}})
              timeout (callback {key {k              val
                                      :remote/status :timeout}})))))))

  )


