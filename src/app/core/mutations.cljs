(ns app.core.mutations
  (:require [app.parser :as p]
            [om.next :as om]
            [routom.bidi :as rb]
            [datascript.core :as d]))

(defmethod p/mutate 'go/login
  [{conn :state c :component :as env} key _]
  {:action
   (fn []
     (let [set-route! (om/shared c :set-route!)
           get-route (om/shared c :get-route)]
       (set-route! {:route/id :login :route/params {:redirect (get-route)}})))})

(defmethod p/mutate 'nav/navigate
  [{conn :state c :component :as env} key {:keys [nav-state action]}]
  {:action
   (fn []
     (let [{:keys [type key params]} action
           set-route! (om/shared c :set-route!)]
       (cond
         (or (= type "BackAction") (= type "back") (= type "pop"))
         (let [current-index (get nav-state :nav-state/index)
               current-route (get-in nav-state [:nav-state/children current-index])
               next-index (dec current-index)
               next-route (get-in nav-state [:nav-state/children next-index])]
           (set-route! {:route/id (:nav-state/key next-route) :route/params (or (:nav-state/params next-route) {})})
           (d/transact!
             conn
             [[:db.fn/retractEntity (:db/id current-route)]
              [:db/add (:db/id nav-state) :nav-state/index next-index]]))

         (= type "push")
         (let [new-nav-state (-> (update nav-state :nav-state/index inc)
                                 (update :nav-state/children conj {:db/id            (d/tempid nil)
                                                                   :nav-state/key    key
                                                                   :nav-state/params (or params {})}))]
           (d/transact!
             conn
             [new-nav-state])
           (set-route! {:route/id key :route/params params})))))}
  )