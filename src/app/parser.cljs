(ns app.parser
  (:require [om.next :as om]
            [datascript.core :as d]))

(defmulti read om/dispatch)

(defmethod read :default
  [_ _ _]
  {:value {}})

(defmulti mutate om/dispatch)

(defmethod mutate 'remote/force
  [env key params]
  {:action #()})

(def parser (om/parser {:read read :mutate mutate}))

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

(defn update-ast
  [{:keys [ast parser] :as env} params]
  (let [{:keys [login]} (parser env [{:login [:login/token]}])]
    (when-let [token (:login/token login)]
      (update ast :params merge {:login/token token} params))))

(defn remote-with-token
  [{:keys [ast parser] :as env} params]
  (let [{:keys [login]} (parser env [{:login [:login/token]}])]
    (when-let [token (:login/token login)]
      {:value :loading
       :remote (update ast :params merge {:login/token token} params)})))

(defn read-remote-with-token
  [{query :query conn :state :as env} [key id :as ident] params]
  (let [entities (pull-by-attr-value @conn key id query)
        value (first entities)]
    ; TODO throw an exception if entities has more than one item
    (if value
      {:value value}
      (remote-with-token env params))))

(defn read-targeted-remote-with-token
  [{{:keys [target] :as ast} :ast query :query parser :parser conn :state :as env} [key val :as ident] params]
  (if target
    (let [login (parser env [{:login [:login/token]}])]
      (if-let [token (get-in login [:login :login/token])]
        {:value :loading
         target (-> (update ast :params merge {:login/token token} params)
                    (assoc :token token))}
        )
      )
    (let [value (pull-by-attr-value @conn key val query)]
      {:value (first value)})
    ))

(defn remote-read
  [id {state :state {target :target} :ast :as env} key params on-success]
  (letfn [(send []
            (let [remote (update-ast env (assoc params :remote/id id))]
              {:value  [{:remote/status :loading} nil]
               :remote remote}))]
    (if target
      (send)
      (let [entities (pull-by-attr-value @state :remote/by-id (into [key] (if (sequential? id) id [id]))  '[*])
            {:keys [remote/status] :as remote-value} (first entities)]

        ; TODO throw an exception if entities has more than one item
        (if remote-value
          (condp = status
            :success (let [v (on-success)]
                       {:value [remote-value v]})
            {:value [remote-value nil]})
          (send))))))