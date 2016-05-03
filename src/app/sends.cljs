(ns app.sends
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :as async]
            [fetch.core :as f]
            [github.core :as ghc]))

(defmulti send (fn [key ast callback] key))

(def request-response-cache (atom {}))

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
      (let [[etag last-modified cached-result] (get @request-response-cache request)
            request (cond-> request
                            etag (assoc-in [:headers "If-None-Match"] etag)
                            last-modified (assoc-in [:headers "If-Modified-Since"] last-modified))
            [success error timeout :as channels] (f/fetch-json request)]
        (go
          (let [[value ch] (async/alts! channels)]
            (condp = ch
              success (let [{:keys [json ok status response get-header] :as result} value
                            etag (get-header "ETag")
                            last-modified (get-header "Last-Modified")]
                        (if (= status 304)
                          (let [{:keys [json ok status response get-header]} cached-result]
                            (callback {key (response->txData cached-result)})
                            (callback {key {k              val
                                            :remote/status :success
                                            :http/status   status
                                            :http/ok       ok
                                            :http/json     json}}))
                          (do
                            (when (or etag last-modified)
                              (swap! request-response-cache assoc request [etag last-modified result]))
                            (callback {key (response->txData value)})
                            (callback {key {k              val
                                            :remote/status :success
                                            :http/status   status
                                            :http/ok       ok
                                            :http/json     json}}))))
              error (callback {key {k              val
                                    :remote/status :error
                                    :remote/error  value}})
              timeout (callback {key {k              val
                                      :remote/status :timeout}})))))))

  )


