(ns graf-znak.bench
  (:require [graf-znak.core :refer :all]
            [criterium.core :refer :all]
            [graf-znak.atom-storage :as atom-store]
            [graf-znak.concurrent-hash-storage :as chash-store]
            [graf-znak.accumulators :as accums]
            [graf-znak.hooks :as hooks]))

(defn- run
  [factory map-fn inputs hooks]
  (bench
   (let [net (create-net hooks factory)]
     (doall
      (map-fn (partial put net) inputs)))))

(defn run-atom-single
  [inputs groups]
  (run atom-store/factory map inputs
       [(hooks/->Hook groups [accums/counter])]))

(defn run-atom-parallel
  [inputs groups]
  (run atom-store/factory pmap inputs
       [(hooks/->Hook groups [accums/counter])]))

(defn run-chash-single
  [inputs groups]
  (run chash-store/factory map inputs
       [(hooks/->Hook groups [accums/stateful-counter])]))

(defn run-chash-parallel
  [inputs groups]
  (run chash-store/factory pmap inputs
       [(hooks/->Hook groups [accums/stateful-counter])]))
