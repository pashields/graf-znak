(ns graf-znak.annotations
  "Contains extra annotations of non graf-znak functions."
  (:require [clojure.core.typed :refer :all]))

(ann ^:no-check clojure.core/not-any? (Fn [(Fn [Any -> Boolean]) (Seq Any)
                                           -> Boolean]))
(ann ^:no-check clojure.core.reducers/map
     (All [x y]
          (Fn [(Fn [x -> Any]) (Seq x) -> (Seq Any)])))
(ann ^:no-check clojure.core.reducers/filter
     (Fn [(Fn [Any -> Boolean]) (Seq Any) -> (Seq Any)]))
