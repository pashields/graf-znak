(ns graf-znak.hook-storage
  (:require [clojure.core.typed :refer :all]))

(ann-protocol HookStorage
              inc-hook [HookStorage (Coll Any) -> Any]
              get-groups [HookStorage -> (Map (Coll Any) Number)])
(defprotocol> HookStorage
  (inc-hook 
   [storage group] 
   "Increment or initialize the count for a given group")
  (get-groups
   [storage]
   "Retrieves all known groups and their counts"))
