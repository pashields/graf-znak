(ns graf-znak.atom-storage
  (:require [clojure.core.typed :refer :all]
            [graf-znak.hook-storage :refer :all]))

(defn> safe-update
  :- (Map (Coll Any) Number)
  [coll :- (Map (Coll Any) Number) k :- (Coll Any)]
  (let [new-val (inc (get coll k 0))]
    (assoc coll k new-val)))

(ann-datatype AtomStorage [storage :- (Atom1 (Map (Coll Any) Number))])
(deftype AtomStorage [storage]
  HookStorage
  (inc-hook
    [this group]
    (swap! storage safe-update group))
  (get-groups
    [this]
    @storage))

(defn> factory
  :- AtomStorage
  []
  (let [base {}]
    (AtomStorage. (atom (ann-form base (Map (Coll Any) Number))))))
