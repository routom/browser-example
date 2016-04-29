(ns github.core
  (:require [clojure.string :as str]))

(defprotocol IGitHub
  (get-endpoint [x])
  (GET-json-request [x path-and-query])
  (GET-current-user-request [x])
  (full-url [this path-and-query]))

(defrecord GitHub [endpoint]
  IGitHub
  (get-endpoint [_] endpoint)

  (full-url
    [this path-and-query]
    (str (get-endpoint this) path-and-query))
  (GET-json-request [this path-and-query]
    {:url (full-url this path-and-query)
     :method "GET"
     :headers {"Accept" "application/json"}})
  (GET-current-user-request [this]
    (GET-json-request this "/user")))

(defn parse-link [link]
  (let [[_ url] (re-find #"<(.*)>" link)
        [_ rel] (re-find #"rel=\"(.*)\"" link)]
    [(keyword rel) url]))

(defn parse-links
  "Takes the content of the link header from a github resp, returns a map of links"
  [link-body]
  (->> (str/split link-body #",")
       (map parse-link)
       (into {})))