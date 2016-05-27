(ns app.components)

(set! js/window.base64 (js/require "base-64"))
(set! js/window.React (js/require "react"))
(set! js/window.ReactNative (js/require "react-native"))
(def ReactNative js/ReactNative)
(def React js/React)
(def cf (.-createFactory React))
(def view (cf (.-View ReactNative)))
(def scroll-view (cf (.-ScrollView ReactNative)))
(def text-input (cf (.-TextInput ReactNative)))
(def text (.createFactory React (.-Text ReactNative)))
(def touchable-highlight (.createFactory React (.-TouchableHighlight ReactNative)))
(def picker (.createFactory React (.-Picker ReactNative)))
(def picker-item (.createFactory React (.. ReactNative -Picker -Item)))
(def list-view (js/React.createFactory js/ReactNative.ListView))

(defn render-list-view
  [coll render-row]
  (let [data-source (js/ReactNative.ListView.DataSource. #js {
                                                        :sectionHeaderHasChanged #(not (= %1 %2))
                                                        :rowHasChanged #(not (= %1 %2))
                                                        :getSectionHeaderData (fn [dataBlob sectionId] dataBlob)
                                                        :getRowData (fn [dataBlob, sectionId, rowId]
                                                                      (get dataBlob rowId))
                                                        })
        data-source' (.cloneWithRowsAndSections data-source coll #js ["s1"] #js [(cljs.core/clj->js (range 0 (count coll)))])]
    (list-view #js {:dataSource data-source'
                    :renderRow render-row}))
  )

(def navigation-animated-view (.createFactory React (.. ReactNative -NavigationExperimental -AnimatedView)))
(def navigation-card (.createFactory React (.. ReactNative -NavigationExperimental -Card)))
(def NavigationHeader (.. ReactNative -NavigationExperimental -Header))
(def navigation-header (.createFactory React NavigationHeader))
(def navigation-header-title (.createFactory React (.. ReactNative -NavigationExperimental -Header -Title)))


(def div view)
(def a touchable-highlight)
(def label text)
(defn button
  [props child]
  (touchable-highlight (clj->js (merge {:style {:backgroundColor "#660"
                                                :padding         10
                                                :borderRadius    5}} props)) child))
(def input text-input)
(def p text)
(def span text)
(def h1 text)
(def select picker)
(def option picker-item)
(def pre text)
(def ul view)
(def li view)
(def h2 text)