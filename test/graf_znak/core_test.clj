(ns graf-znak.core-test
  (:require [clojure.test :refer :all]
            [graf-znak.core :refer :all]
            [graf-znak.hooks :refer :all]
            [graf-znak.atom-storage :as atom-storage]
            ;[graf-znak.concurrent-hash-storage :as concurrent-hash-storage]
            [simple-check.core :as sc]
            [simple-check.generators :as gen]
            [simple-check.properties :as prop]
            [simple-check.clojure-test :refer :all]))

(def counter 
  (->Accumulator :count
                 (fn [state input]
                   (let [state (or state 0)]
                     (inc state)))))

(defn count-inputs
  [colls inputs]
  (frequencies (map #(map (fn [k] (get % k)) colls) inputs)))

(defn gen-maps
  [colls]
  (gen/map (gen/elements colls)
           (gen/choose 0 10)))

(defn gen-inputs
  [colls]
  (gen/vector (gen/such-that #(= (count colls)
                                 (count %))
                             (gen-maps colls))))

(defn gen-hook
  [max-dim]
  (gen/not-empty
   (gen/resize max-dim
    (gen/list
     (gen/not-empty
      gen/string-ascii)))))

(defn freq-groups
  [hook-colls inputs]
  (count-inputs hook-colls inputs))

(defn graf-groups
  [factory hook-colls inputs]
  (let [hook (->Hook hook-colls [counter])
        net (create-net [hook] factory)]
    (doseq [i inputs] (put net i))
    (let [hook-results (check net hook)]
      (into {} (map (fn [[k v]] [k (:count v)]) hook-results)))))

(defn check-inputs
  [factory hook-colls inputs]
  (= (graf-groups factory hook-colls inputs) 
     (freq-groups hook-colls inputs)))

(defn hook-prop-factory
  [storage-factory]
  (let [hook-colls [:a :b]]
    (prop/for-all [inputs (gen-inputs hook-colls)]
                  (check-inputs storage-factory hook-colls inputs))))

(binding [*report-trials* true]
  (defspec atom-basic 1000 (hook-prop-factory atom-storage/factory))
  ;(defspec chash-basic 1000 (hook-prop-factory concurrent-hash-storage/factory))
  )
