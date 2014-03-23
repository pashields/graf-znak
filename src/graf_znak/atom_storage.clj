(ns graf-znak.atom-storage
  "Exposes the AtomStorage type which implements an atom backed HookStorage."
  (:require [clojure.core.typed :refer :all]
            [graf-znak.hooks :refer :all]
            [graf-znak.accumulators :refer :all]))

(def-alias inner-storage (Map group-type
                              (Map accumulator-name-type
                                   accumulator-state-type)))

(defn> safe-update
  "Runs a given accumulator on a given record. Meant to be called inside
   swap or other STM-like."
  :- inner-storage
  [coll :- inner-storage
   k :- group-type
   record :- input-type
   accumulator :- accumulator-type]
  (let [{accum-name :name init :initial-state accum-fn :fn} accumulator
        old (get coll k {})
        old-accum (get old accum-name init)
        new-accum (accum-fn old-accum record)]
    (assoc coll k (assoc old accum-name new-accum))))

(ann-datatype AtomStorage [storage :- (Atom1 inner-storage)])
(deftype AtomStorage [storage]
  HookStorage
  (accumulate-hook
    [this group record accumulator]
    (swap! storage safe-update group record accumulator))
  (get-groups
    [this]
    @storage))

(defn> factory
  "Creates a new instance of atom storage."
  :- AtomStorage
  []
  (let [base {}]
    (AtomStorage. (atom (ann-form base inner-storage)))))
