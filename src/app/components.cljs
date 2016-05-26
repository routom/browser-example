(ns app.components)

(set! js/window.base64 (js/require "base-64"))
(set! js/window.React (js/require "react-native"))
(def React (js/require "react-native"))
(def view (.createFactory React (.-View React)))
(def scroll-view (.createFactory React (.-ScrollView React)))
(def text-input (.createFactory React (.-TextInput React)))
(def text (.createFactory React (.-Text React)))
(def touchable-highlight (.createFactory React (.-TouchableHighlight React)))
(def picker (.createFactory React (.-Picker React)))
(def picker-item (.createFactory React (.. React -Picker -Item)))
(def list-view (js/React.createFactory js/React.ListView))

(defn render-list-view
  [coll render-row]
  (let [data-source (js/React.ListView.DataSource. #js {
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

(def navigation-animated-view (.createFactory React (.. React -NavigationExperimental -AnimatedView)))
(def navigation-card (.createFactory React (.. React -NavigationExperimental -Card)))
(def NavigationHeader (.. React -NavigationExperimental -Header))
(def navigation-header (.createFactory React NavigationHeader))
(def navigation-header-title (.createFactory React (.. React -NavigationExperimental -Header -Title)))


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