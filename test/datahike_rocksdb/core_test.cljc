(ns datahike-rocksdb.core-test
  (:require
   #?(:cljs [cljs.test    :as t :refer-macros [is deftest]]
      :clj  [clojure.test :as t :refer        [is deftest]])
   [datahike.api :as d]
   [datahike-rocksdb.core]))



(deftest ^:integration test-rocksdb
  (let [config {:store {:backend :rocksdb
                        :path   "/tmp/datahike-rocksdb-test"
                        :scope   "local"}
                :schema-flexibility :read
                :keep-history? false}
        _ (d/delete-database config)]
    (is (not (d/database-exists? config)))
    (let [_ (d/create-database config)
          conn (d/connect config)]

      (d/transact conn [{:db/id 1, :name  "Ivan", :age   15}
                        {:db/id 2, :name  "Petr", :age   37}
                        {:db/id 3, :name  "Ivan", :age   37}
                        {:db/id 4, :age 15}])
      (is (= (d/q '[:find ?e :where [?e :name]] @conn)
             #{[3] [2] [1]}))

      (d/release conn)
      (is (d/database-exists? config))
      (d/delete-database config)
      (is (not (d/database-exists? config))))))
