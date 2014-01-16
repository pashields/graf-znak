(ns graf-znak.core-test
  (:require [clojure.test :refer :all]
            [graf-znak.core :refer :all]
            [graf-znak.atom-storage :as atom-storage]
            [simple-check.core :as sc]
            [simple-check.generators :as gen]
            [simple-check.properties :as prop]
            [simple-check.clojure-test :refer :all]))

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
  [hook-colls inputs]
  (let [net (create-net [hook-colls] atom-storage/factory)]
    (doseq [i inputs] (send-net net i))
    (check-net net hook-colls)))

(defn check-inputs
  [hook-colls inputs]
  (= (graf-groups hook-colls inputs) 
     (freq-groups hook-colls inputs)))

(def hook-prop
  (let [hook-colls [:a :b]]
    (prop/for-all [inputs (gen-inputs hook-colls)]
                  (check-inputs hook-colls inputs))))

(binding [*report-trials* true]
 (defspec basic 1000 hook-prop))
