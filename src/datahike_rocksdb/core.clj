(ns datahike-rocksdb.core
  (:require [datahike.store :refer [empty-store delete-store connect-store default-config config-spec release-store store-identity]]
            [datahike.config :refer [map-from-env]]
            [datahike.tools :as dt]
            [datahike.api :as d]
            [konserve-rocksdb.core :as k]
            [clojure.spec.alpha :as s]
            [zufall.core :refer [rand-german-mammal]]))

(defmethod store-identity :rocksdb [store-config]
  [:rocksdb (:scope store-config) (:path store-config)])

(defmethod empty-store :rocksdb [store-config]
  (k/connect-rocksdb-store (:path store-config)))

(defmethod delete-store :rocksdb [store-config]
  (prn "store" (:store @(:wrapped-atom (d/connect {:store store-config}))))
  (k/release-rocksdb (:store @(:wrapped-atom (d/connect {:store store-config}))))
  (k/delete-rocksdb-store (:path store-config)))

(defmethod connect-store :rocksdb [store-config]
  (k/connect-rocksdb-store (:path store-config)))

(defmethod default-config :rocksdb [config]
  (merge
   (map-from-env :datahike-store-config {:path (str "/tmp/datahike-db-" (rand-german-mammal)) :scope (dt/get-hostname)})
   config))

(s/def :datahike.store.rocksdb/backend #{:rocksdb})
(s/def ::rocksdb (s/keys :req-un [:datahike.store.rocksdb/backend]))

(defmethod config-spec :rocksdb [_] ::rocksdb)

(defmethod release-store :rocksdb [_ store]
  (k/release-rocksdb store))
