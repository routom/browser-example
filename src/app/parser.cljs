(ns app.parser
  (:require [om.next :as om]
            [datascript.core :as d]))

(defmulti read om/dispatch)

(defmulti mutate om/dispatch)

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


(defn read-remote-with-token
  [{ast :ast query :query conn :state :as env} [key val :as ident] params]
  (let [entities (pull-by-attr-value @conn key val query)
        value (first entities)]
    ; TODO throw an exception if entities has more than one item
    (if value
      {:value value}
      (let [{:keys [login]} (parser env [{:login [:login/token]}])]
        (when-let [token (:login/token login)]
          {:value :loading
           :remote (update ast :params merge {:login/token token} params)})))))

(defn read-targeted-remote-with-token
  [{{:keys [target] :as ast} :ast query :query parser :parser conn :state :as env} [key val :as ident] params]
  (if target
    (let [login (parser env [{:login [:login/token]}])]
      (if-let [token (get-in login [:login :login/token])]
        {:value :loading
         target (update ast :params merge {:login/token token} params)}
        )
      )
    (let [value (pull-by-attr-value @conn key val query)]
      {:value (first value)})
    ))