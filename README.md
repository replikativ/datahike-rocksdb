# Datahike RocksDB Backend

<p align="center">
<a href="https://clojurians.slack.com/archives/CB7GJAN0L"><img src="https://img.shields.io/badge/clojurians%20slack-join%20channel-blueviolet"/></a>
<a href="https://clojars.org/io.replikativ/datahike-rocksdb"> <img src="https://img.shields.io/clojars/v/io.replikativ/datahike-rocksdb.svg" /></a>
<a href="https://circleci.com/gh/replikativ/datahike-rocksdb"><img src="https://circleci.com/gh/replikativ/datahike-rocksdb.svg?style=shield"/></a>
<a href="https://github.com/replikativ/datahike-rocksdb/tree/main"><img src="https://img.shields.io/github/last-commit/replikativ/datahike-rocksdb/main"/></a>
</p>

The goal of this backend is to support [RocksDB](https://rocksdb.org). This implementation is a work in progress.

## Configuration
Please read the [Datahike configuration docs](https://github.com/replikativ/datahike/blob/master/doc/config.md) on how to configure your backend. Details about the backend configuration can be found in [konserve-rocksdb](https://github.com/replikativ/konserve-rocksdb).A sample configuration is
`create-database`, `connect` and `delete-database`:
```clojure
{:store {:backend :rocksdb :path "/tmp/datahike-rocksdb-playground"}}
```

## Usage
Add to your Leiningen or Boot dependencies:
[![Clojars Project](https://img.shields.io/clojars/v/io.replikativ/datahike-rocksdb.svg)](https://clojars.org/io.replikativ/datahike-rocksdb)

### Run Datahike in your REPL
```clojure
  (ns project.core
    (:require [datahike.api :as d]
              [datahike-rocksdb.core]))

  (def cfg {:store {:backend :rocksdb :path "/tmp/datahike-rocksdb-playground"}})

  ;; Create a database at this place, by default configuration we have a strict
  ;; schema validation and keep historical data
  (d/create-database cfg)

  (def conn (d/connect cfg))

  ;; The first transaction will be the schema we are using:
  (d/transact conn [{:db/ident :name
                     :db/valueType :db.type/string
                     :db/cardinality :db.cardinality/one }
                    {:db/ident :age
                     :db/valueType :db.type/long
                     :db/cardinality :db.cardinality/one }])

  ;; Let's add some data and wait for the transaction
  (d/transact conn [{:name  "Alice", :age   20 }
                    {:name  "Bob", :age   30 }
                    {:name  "Charlie", :age   40 }
                    {:age 15 }])

  ;; Search the data
  (d/q '[:find ?e ?n ?a
         :where
         [?e :name ?n]
         [?e :age ?a]]
    @conn)
  ;; => #{[3 "Alice" 20] [4 "Bob" 30] [5 "Charlie" 40]}

  ;; Clean up the database if it is not needed any more
  (d/delete-database cfg)
```

## Run Tests

```bash
  bash -x ./bin/run-integration-tests
```

## License

Copyright © 2023 lambdaforge UG (haftungsbeschränkt)

This program and the accompanying materials are made available under the terms of the Eclipse Public License 1.0.
