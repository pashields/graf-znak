(ns graf-znak.core
  (:require [clojure.core.typed :refer :all]
            [clojure.core.reducers :as r]
            [graf-znak.hooks :refer :all]))

;; Type aliases
(def-alias hooks-type (Seq hook-type))
(def-alias state-type (Map hook-type
                           HookStorage))

;; Annotations
(ann ^:no-check clojure.core/not-any? (Fn [(Fn [Any -> Boolean]) (Seq Any)
                                           -> Boolean]))
(ann ^:no-check clojure.core.reducers/map
     (All [x y]
          (Fn [(Fn [x -> Any]) (Seq x) -> (Seq Any)])))
(ann ^:no-check clojure.core.reducers/filter
     (Fn [(Fn [Any -> Boolean]) (Seq Any) -> (Seq Any)]))

(defn> process-hook
  "Processes a single input for all accumulators in a hook."
  :- Any
  [state :- state-type hook :- hook-type val :- input-type]
  (let [{fields :fields accums :accumulators} hook
        hook-storage (get state hook)
        group (map #(get val %) fields)]
    (assert (not (nil? hook-storage)))
    (when (not-any? nil? group)
      (doseq> [accum :- accumulator-type accums]
        (accumulate-hook hook-storage group val accum)))))

(defn> process
  "Processes a single input for n hooks"
  :- Number
  [hooks :- hooks-type state :- state-type val :- input-type]
  (count
   (into [] (r/filter (fn> :- Boolean
                           [x :- Any]
                           (not (nil? x)))
                      (r/map (fn> :- Any
                                  [hook :- hook-type]
                                  (process-hook state hook val)) hooks)))))

(defn> check-hook
  "Returns all groups and their respective accumulations for a given hook."
  :- hook-result-type
  [state :- state-type hook :- hook-type]
  (let [hook-storage (get state hook)]
    (assert (not (nil? hook-storage)))
    (get-groups hook-storage)))

(def-alias net-type (HMap :mandatory {:hooks hooks-type :state state-type}))

(defn> create-net
  "Generates a new net. A net is a stateful datum that can be used to run
   accumulations over groups of data."
  :- net-type
  [hooks :- hooks-type storage-factory :- (Fn [-> HookStorage])]
  (let [state (zipmap hooks (repeatedly storage-factory))]
    {:hooks hooks :state state}))

(defn> check-net
  "Get the current groups and their accumulations for a given hook."
  :- hook-result-type
  [net :- net-type hook :- hook-type]
  (check-hook (:state net) hook))

(defn> send-net
  "Update a net with a new value."
  :- Number
  [net :- net-type val :- input-type]
  (process (:hooks net) (:state net) val))
