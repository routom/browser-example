(ns react-native.linking)

(def React (js/require "react-native"))


(def linking (.-Linking React))

(defn open-url
  [url]
  (.openURL linking url))