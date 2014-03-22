(ns graf-znak.hooks
  "Contains the records and protocols related to hooks."
  (:require [clojure.core.typed :refer :all]
            [graf-znak.accumulators :refer :all]))

;; Hook
;; Hooks are collections of accumulators that will be run on groups of the
;; given fields. So for every unique combination of values from fields,
;; all the given accumulators will be run. A hook should not contain multiple
;; accumulators with the same name.
(def-alias fields-type (Coll (U Keyword String)))

(ann-record Hook [fields :- fields-type
                  accumulators :- (Coll accumulator-type)])
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
              accumulate-hook [HookStorage group-type input-type accumulator-type
                               -> accumulator-state-type]
              get-groups [HookStorage -> hook-result-type])
(defprotocol> HookStorage
  (accumulate-hook
   [storage group record accumulator]
   "Increment or initialize the count for a given group")
  (get-groups
   [storage]
   "Retrieves all known groups and their counts"))
