(ns app.auth.schema)

(def schema
  {:login {:db/unique :db.unique/identity}
   :user/login {:db/unique :db.unique/identity}
   :user/current {:db/unique :db.unique/identity}
   :user/user {:db/valueType :db.type/ref
               :db/cardinality :db.cardinality/one}})

