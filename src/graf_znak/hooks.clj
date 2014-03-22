(ns graf-znak.hooks
  "Contains the records and protocols required by graf-znak."
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
             fn :- accumulator-fn-type])
(defrecord Accumulator [name fn])
(def-alias accumulator-type Accumulator)

;; Hook
;; Hooks are collections of accumulators that will be run on groups of the
;; given fields. So for every unique combination of values from fields,
;; all the given accumulators will be run. A hook should not contain multiple
;; accumulators with the same name.
(def-alias fields-type (Coll (U Keyword String)))

(ann-record Hook [fields :- fields-type
                  accumulators :- (Coll Accumulator)])
(defrecord Hook [fields accumulators])
(def-alias hook-type Hook)

;; Hook-storage
;; Specifies a protocol that must be fulfilled by backing stores in order to be
;; used as backing storage for graf-znak. The storage should implement it's own
;; thread safety.
(def-alias group-type (Coll Any))
(def-alias hook-result-type (Map group-type
                                 (Map accumulator-name-type
                                      accumulator-state-type)))

(ann-protocol HookStorage
              accumulate-hook [HookStorage group-type input-type Accumulator
                               -> accumulator-state-type]
              get-groups [HookStorage -> hook-result-type])
(defprotocol> HookStorage
  (accumulate-hook
   [storage group record accumulator]
   "Increment or initialize the count for a given group")
  (get-groups
   [storage]
   "Retrieves all known groups and their counts"))
