(ns graf-znak.core
  (:require [clojure.core.typed :refer :all]
            [clojure.core.reducers :as r]
            [graf-znak.hooks :refer :all]
            [graf-znak.accumulators :refer :all]
            [graf-znak.annotations :refer :all]))

;; Type aliases
(def-alias hooks-type (Seq hook-type))
(def-alias state-type (Map hook-type
                           HookStorage))

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

(ann-record Net [hooks :- hooks-type state :- state-type])
(defrecord Net [hooks state])
(def-alias net-type Net)

(defn> create-net
  "Generates a new net. A net is a stateful datum that can be used to run
   accumulations over groups of data."
  :- Net
  [hooks :- hooks-type storage-factory :- (Fn [-> HookStorage])]
  (let [state (zipmap hooks (repeatedly storage-factory))]
    (->Net hooks state)))

(defn> check
  "Get the current groups and their accumulations for a given hook."
  :- hook-result-type
  [net :- Net hook :- hook-type]
  (check-hook (:state net) hook))

(defn> put
  "Update a net with a new value."
  :- Number
  [net :- Net val :- input-type]
  (process (:hooks net) (:state net) val))
