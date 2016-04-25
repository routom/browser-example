(ns app.repos.schema)

(def schema {:repos/list {:db/unique :db.unique/identity}
             :repo/id {:db/unique :db.unique/identity}
             :repo/name {:db/index true}
             :repo/owner {:db/valueType :db.type/ref
                          :db/cardinality :db.cardinality/one}})