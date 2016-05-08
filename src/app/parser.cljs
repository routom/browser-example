(ns app.parser
  (:require [om.next :as om]
            [datascript.core :as d]))

(defn pull-by-attr-value
  ([db key value]
   (pull-by-attr-value db key value '[*]))
  ([db key value query]
   (let [entities (d/q '[:find [(pull ?e ?selector) ...]
                         :in $ ?attr ?value ?selector
                         :where [?e ?attr ?value]]
                       db key value (or query '[*]))]
     entities)))

(defn pull-one-by-attr-value
  [db key value query]
  (first (pull-by-attr-value db key value query)))

(defmulti read om/dispatch)

(defmethod read :default
  [_ _ _]
  {:value {}})

(defmethod read :login
  [{:keys [state query]} key _]
  (let [entities (pull-by-attr-value @state key true query)
        value (first entities)]
    (if value
      {:value value})))

(defmulti mutate om/dispatch)

(defmethod mutate 'remote/force
  [env key params]
  {:action #()})

(def parser (om/parser {:read read :mutate mutate}))

(defn update-ast
  [{:keys [ast parser] :as env} params]
  (let [{:keys [login]} (parser env [{:login [:login/token]}])]
    (when-let [token (:login/token login)]
      (update ast :params merge {:login/token token} params))))

(defn ast-with-token
  [{:keys [ast parser] :as env}]
  (let [{:keys [login]} (parser env [{:login [:login/token]}])]
    (if-let [token (:login/token login)]
      (update ast :params merge {:login/token token})
      ast)))

(defn- remote-read*
  [target-only? id {state :state {target :target} :ast :as env} key params on-success]
  (letfn [(send []
            (let [remote (update-ast env (assoc params :remote/id id))]
              {:value  [{:remote/status :loading} nil]
               :remote remote}))]
    (if target
      (send)
      (let [entities (pull-by-attr-value @state :remote/by-id (into [key] (if (sequential? id) id [id])) '[*])
            {:keys [remote/status] :as remote-value} (first entities)]

        (if remote-value
          (condp = status
            :success (let [v (on-success)]
                       {:value [remote-value v]})
            {:value [remote-value nil]})
          (if-not target-only?
            (send)))))))

(defn remote-read
  [id {state :state {target :target} :ast :as env} key params on-success]
  (remote-read* false id env key params on-success))

(defn remote-forced-read
  [id {state :state {target :target} :ast :as env} key params on-success]
  (remote-read* true id env key params on-success))