(ns graf-znak.accumulators
  "Contains the accumulator record and types as well as pre-built instances."
  (:require [clojure.core.typed :refer :all]))

;; Accumulator
;; Accumulators form the basis of all graf-znak operations. The name of the
;; accumulator should be unique, as it may be used by storage. The
;; accumulator function should handle an initial state type of nil. Whatever
;; is returned by the function will be stored as state for the next execution.
;; Accumulator functions should be thread-safe.
(def-alias accumulator-name-type (U String Keyword))
(def-alias accumulator-state-type Any)
(def-alias input-type (Map (U Keyword String) Any))
(def-alias accumulator-fn-type (Fn [accumulator-state-type input-type ->
                                    accumulator-state-type]))

(ann-record Accumulator
            [name :- accumulator-name-type
             initial-state :- accumulator-state-type
             fn :- accumulator-fn-type])
(defrecord Accumulator [name initial-state fn])
(def-alias accumulator-type Accumulator)

;; Prebuilt Accumulators
(ann counter Accumulator)
(def counter
  (->Accumulator
   :count
   0
   (fn> :- AnyInteger
        [state :- Any
         _ :- input-type]
        (assert (integer? state))
        (inc state))))

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
   #{}
   (fn> :- (Set (Coll Any))
        [state :- Any
         input :- input-type]
        (let [vals (map #(get input %) fields)]
          ;; As of this writing pred requires fully qualified symbols if check-ns
          ;; is called from another namespace. blech.
          (assert ((pred (clojure.core.typed/Set (clojure.core.typed/Coll Any))) state))
          (conj state (into {} (interleave fields vals)))))))
