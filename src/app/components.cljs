(ns app.components)

(set! js/window.React (js/require "react-native"))
(def React (js/require "react-native"))
(def view (.createFactory React (.-View React)))
(def text-input (.createFactory React (.-TextInput React)))
(def text (.createFactory React (.-Text React)))
(def touchable-highlight (.createFactory React (.-TouchableHighlight React)))

(def div view)
(def a touchable-highlight)
(def label text)
(def button touchable-highlight)
(def input text-input)
(def p text)
(def span text)
(def h1 text)
(def select text-input)
(def option text)
(def pre text)
(def ul view)
(def li view)
(def h2 text)