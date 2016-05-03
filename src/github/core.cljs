(ns github.core
  (:require [clojure.string :as str]))

(def ^:dynamic *github-api-endpoint* "https://api.github.com")

(defn full-url
  [path-and-query]
  (str *github-api-endpoint* path-and-query))

(defn GET-json-request [path-and-query]
                  {:url (full-url path-and-query)
                   :method "GET"
                   :headers {"Accept" "application/json"}})

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