(ns graf-znak.accumulators
  "Contains the accumulator record and types as well as pre-built instances."
  (:require [clojure.core.typed :refer :all]
            [graf-znak.annotations :refer :all])
  (:import [java.util.concurrent.atomic AtomicLong]))

;; Accumulator
;; Accumulators form the basis of all graf-znak operations. The name of the
;; accumulator should be unique, as it may be used by storage. The
;; accumulator function should handle an initial state type of nil. Whatever
;; is returned by the function will be stored as state for the next execution.
;; Accumulator functions should be thread-safe.
(def-alias accumulator-name-type (U String Keyword))
(def-alias accumulator-state-type Any)
(def-alias accumulator-init-fn-type (Fn [-> accumulator-state-type]))
(def-alias input-type (Map (U Keyword String) Any))
(def-alias accumulator-fn-type (Fn [accumulator-state-type input-type ->
                                    accumulator-state-type]))

(ann-record Accumulator
            [name :- accumulator-name-type
             init-fn :- accumulator-init-fn-type
             fn :- accumulator-fn-type])
(defrecord Accumulator [name init-fn fn])
(def-alias accumulator-type Accumulator)

;; Prebuilt Accumulators
(ann counter Accumulator)
(def counter
  (->Accumulator
   :count
   (fn [] 0)
   (fn> :- AnyInteger
        [state :- Any
         _ :- input-type]
        (assert (integer? state))
        (inc state))))

(ann stateful-counter Accumulator)
(def stateful-counter
  (->Accumulator
   :count
   (fn [] (AtomicLong.))
   (fn> :- (Value nil)
        [state :- Any
         _ :- input-type]
        (assert (instance? AtomicLong state))
        (.incrementAndGet ^AtomicLong state)
        nil)))

(defn> unique-factory
  "Creates an accumulator that will hold all unique combinations of specified
   fields. For instance, if you have records like

   {:name 'Pat Shields' :address '500 W Main St' :age 28 :gender :male}

   You could find the unique age/gender pairs for every address by creating a
   hook on the address field and a unique accumulator on the age and gender
   fields."
  :- Accumulator
  [name :- accumulator-name-type fields :- (Coll Any)]
  (->Accumulator
   name
   (fn [] #{})
   (fn> :- (Set (Coll Any))
        [state :- Any
         input :- input-type]
        (let [vals (map #(get input %) fields)]
          (assert ((pred (Set (Coll Any))) state))
          (if (not-any? nil? vals)
            (conj state (zipmap fields vals))
            state)))))
