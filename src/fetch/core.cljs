(ns fetch.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.Uri.QueryData :as q]
            [cljs.core.async :as async :refer [chan timeout close! >! <! ]])
  (:import goog.Uri.QueryData))

(def ^:dynamic *timeout* (* 10 1000))

(defn fetch-json
  ([{:keys [url method timeout-ms] :as opts}]
   (let [success-chan (chan)
         error-chan (chan)
         timeout-chan (timeout (or timeout-ms *timeout*))
         opts (dissoc opts :url)
         opts-js (if (= method "POST")
                   (clj->js (update opts :body #(js/JSON.stringify (clj->js %))))
                   (clj->js opts))
         close-chans (fn []
                       (async/close! success-chan)
                       (async/close! error-chan))]
     (-> (js/fetch url opts-js)
         (.then (fn [response]

                  (-> (.json response)
                      (.then (fn [json]
                               (let [headers (.-headers response)
                                     json-response {:json (when json (js->clj json :keywordize-keys true))
                                                    :get-header (fn [name] (.get headers name))
                                                    :status (.-status response)
                                                    :ok (.-ok response)
                                                    :response response}]
                                 (async/put! success-chan json-response)))))))
         (.catch (fn [result]
                   (if (instance? js/Error result)
                     (async/put! error-chan result)
                     (async/put! error-chan (js->clj result :keywordize-keys true)))))
         (.then (fn [result]
                  (close-chans))
                (fn [error]
                  (close-chans))))
     [success-chan error-chan timeout-chan])
    ))

(defn map->qs [map]
  (-> (clj->js map)
      (q/createFromMap)
      (.toString)))