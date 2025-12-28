(ns datahike-rocksdb.core
  (:require [datahike.store :refer [empty-store delete-store connect-store default-config config-spec release-store store-identity]]
            [datahike.config :refer [map-from-env]]
            [datahike.tools :as dt]
            [konserve-rocksdb.core :as k]
            [clojure.spec.alpha :as s]
            [zufall.core :refer [rand-german-mammal]]))

;; Track open stores to avoid RocksDB lock conflicts
;; RocksDB only allows one process to hold the lock per database directory
(def ^:private open-stores (atom {}))  ;; {path -> store}

(defmethod store-identity :rocksdb [store-config]
  [:rocksdb (:scope store-config) (:path store-config)])

(defmethod empty-store :rocksdb [store-config]
  (let [path (:path store-config)]
    (or (get @open-stores path)
        (let [store (k/connect-rocksdb-store path)]
          (swap! open-stores assoc path store)
          store))))

(defmethod delete-store :rocksdb [store-config]
  (let [path (:path store-config)]
    ;; Close any open store first
    (when-let [store (get @open-stores path)]
      (swap! open-stores dissoc path)
      (k/release-rocksdb store))
    ;; Now delete the database files
    (try
      (k/delete-rocksdb-store path)
      (catch Exception e
        (println "Failed to delete store" e)))))

(defmethod connect-store :rocksdb [store-config]
  (let [path (:path store-config)]
    (or (get @open-stores path)
        (try
          (let [store (k/connect-rocksdb-store path)]
            (swap! open-stores assoc path store)
            store)
          (catch Exception e
            (println "Failed to connect to store" e)
            nil)))))

(defmethod default-config :rocksdb [config]
  (merge
   (map-from-env :datahike-store-config {:path (str "/tmp/datahike-db-" (rand-german-mammal)) :scope (dt/get-hostname)})
   config))

(s/def :datahike.store.rocksdb/backend #{:rocksdb})
(s/def ::rocksdb (s/keys :req-un [:datahike.store.rocksdb/backend]))

(defmethod config-spec :rocksdb [_] ::rocksdb)

(defmethod release-store :rocksdb [store-config store]
  (let [path (:path store-config)]
    (when (get @open-stores path)
      (swap! open-stores dissoc path)
      (k/release-rocksdb store))))
