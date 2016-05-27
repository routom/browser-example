(ns cljsrn-example.android.core
  (:require-macros [natal-shell.components :refer [view text image touchable-highlight]]
                   [natal-shell.alert :refer [alert]])
  (:require [om.next :as om :refer-macros [defui]]
            [re-natal.support :as sup]
            [app.dev :as main]))

(set! js/window.React (js/require "react"))
(set! js/window.ReactNative (js/require "react-native"))

(def app-registry (.-AppRegistry js/ReactNative))



(defonce RootNode (sup/root-node! 1))
(defonce app-root (om/factory RootNode))

(def app main/app)
(def reconciler (:reconciler app))

(def AppRoot (:root-class app))

(defn init []
      (om/add-root! reconciler AppRoot 1)
      (.registerComponent app-registry "CljsrnExample" (fn [] app-root)))