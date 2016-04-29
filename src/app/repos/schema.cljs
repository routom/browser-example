(ns app.repos.schema)

(def schema {:repos/list {:db/unique :db.unique/identity}
             :tree/by-name {:db/unique :db.unique/identity}
             :repo/detail {:db/unique :db.unique/identity}
             :repo/id {:db/unique :db.unique/identity}
             :repo/branches {:db/valueType :db.type/ref
                             :db/cardinality :db.cardinality/many
                             :db/isComponent true}
             :branch/tree {:db/valueType :db.type/ref
                           :db/cardinality :db.cardinality/one}
             :tree/sha {:db/unique :db.unique/identity}
             :tree/tree {:db/valueType :db.type/ref
                         :db/cardinality :db.cardinality/many
                         :db/isComponent true}
             :tree-item/sha {:db/unique :db.unique/identity}
             :tree-item/path {:db/index true}
             :branch/id {:db/unique :db.unique/identity}
             :repo/name {:db/index true}
             :repo/owner {:db/valueType :db.type/ref
                          :db/cardinality :db.cardinality/one}})