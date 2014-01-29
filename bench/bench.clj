(ns graf-znak.bench
  (:require [graf-znak.core :refer :all]
            [criterium.core :refer :all]
            [graf-znak.atom-storage :as atom-store]
            [graf-znak.concurrent-hash-storage :as chash-store]))

(defn- run
  [factory map-fn inputs groups]
  (bench
   (let [net (create-net groups factory)]
     (doall
      (map-fn (partial send-net net) inputs)))))

(defn run-atom-single
  [inputs groups]
  (run atom-store/factory map inputs groups))

(defn run-atom-parallel
  [inputs groups]
  (run atom-store/factory pmap inputs groups))

(defn run-chash-single
  [inputs groups]
  (run chash-store/factory map inputs groups))

(defn run-chash-parallel
  [inputs groups]
  (run chash-store/factory pmap inputs groups))
