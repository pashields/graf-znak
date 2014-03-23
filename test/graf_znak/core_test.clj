(ns graf-znak.core-test
  (:require [clojure.test :refer :all]
            [graf-znak.core :refer :all]
            [graf-znak.hooks :refer :all]
            [graf-znak.accumulators :refer :all]
            [graf-znak.atom-storage :as atom-storage]
            [graf-znak.concurrent-hash-storage :as concurrent-hash-storage]
            [clojure.test.check :as sc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer :all]))

(defn get-records
  [colls inputs]
  (map #(map (fn [k] (get % k)) colls) inputs))

(defn count-inputs
  [colls inputs]
  (frequencies (get-records colls inputs)))

(defn unique-vals
  [colls hook-colls inputs]
  (let [all-colls (apply conj hook-colls colls)
        records (get-records all-colls inputs)]
    (into {}
          (map
           (fn [[g records]]
             [g (into #{}
                      (map #(apply hash-map
                                   (interleave colls (take-last  (count colls) %)))
                           records))])
           (group-by #(take (count hook-colls) %) records)))))

;; Generators
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

;; Checking duct tape
(defn regular-groups
  [fn hook-colls inputs]
  (fn hook-colls inputs))

(defn graf-groups
  [factory hook-colls inputs accum]
  (let [hook (->Hook hook-colls [accum])
        net (create-net [hook] factory)]
    (doseq [i inputs] (put net i))
    (let [hook-results (check net hook)]
      (into {} (map (fn [[k v]] [k ((:name accum) v)]) hook-results)))))

(defn check-inputs
  [factory hook-colls inputs accum check-fn]
  (= (graf-groups factory hook-colls inputs accum)
     (regular-groups check-fn hook-colls inputs)))

;; Prop builder
(defn hook-prop-builder
  [storage-factory accum check-fn]
  (let [hook-colls [:a :b]]
    (prop/for-all [inputs (gen-inputs hook-colls)]
                  (check-inputs storage-factory hook-colls inputs accum check-fn))))

(def num-trials 1000)
(binding [*report-trials* true]
  (defspec atom-counter num-trials
    (hook-prop-builder atom-storage/factory
                       counter
                       count-inputs))
  (defspec chash-counter num-trials
    (hook-prop-builder concurrent-hash-storage/factory
                       stateful-counter
                       count-inputs))
  (defspec atom-unique num-trials
    (prop/for-all [inputs (gen-inputs [:a :b :c :d])]
                  (check-inputs atom-storage/factory
                                [:a :b :c :d]
                                inputs
                                (unique-factory :uniques [:b])
                                (partial unique-vals [:b]))))
  )
