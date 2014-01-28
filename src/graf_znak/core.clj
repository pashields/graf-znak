(ns graf-znak.core
  (:require [clojure.core.typed :refer :all]
            [clojure.algo.generic.functor :refer :all]
            [clojure.core.reducers :as r]
            [graf-znak.hook-storage :refer :all]))

;; Type aliases
(def-alias hook-type (Coll (U Keyword String)))
(def-alias hooks-type (Seq hook-type))
(def-alias state-type (Map hook-type
                           HookStorage))
(def-alias input-type (Map (U Keyword String) Any))

;; Annotations
(ann ^:no-check clojure.core/not-any? (Fn [(Fn [Any -> Boolean]) (Seq Any) 
                                           -> Boolean]))
(ann ^:no-check clojure.core/alter (Fn [Any * -> Any]))
(ann ^:no-check clojure.algo.generic.functor/fmap
     (All [x y k] 
          (Fn [(Fn [x -> y])
               (Map k x) ->
               (Map k y)])))
(ann ^:no-check clojure.core.reducers/map 
     (All [x y]
          (Fn [(Fn [x -> Any]) (Seq x) -> (Seq Any)])))
(ann ^:no-check clojure.core.reducers/filter
     (Fn [(Fn [Any -> Boolean]) (Seq Any) -> (Seq Any)]))

(defn> process-hook
  "Processes a single input for a single hook"
  :- Any
  [state :- state-type hook :- hook-type val :- input-type]
  (let [storage (get state hook)
        group (map #(get val %) hook)]
    (assert (not (nil? storage)))
    (when (not-any? nil? group)
      (inc-hook storage group))))

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
  "Returns all groups and their respective counts for a given hook."
  :- (Map (Coll Any) Number)
  [state :- state-type hook :- hook-type]
  (let [storage (get state hook)]
    (assert (not (nil? storage)))
    (get-groups storage)))

(def-alias send-type (Fn [input-type -> Number]))
(def-alias check-type (Fn [hook-type -> (Map (Coll Any) Number)]))
(def-alias net-type (HMap :mandatory {:send send-type :check check-type}))

(defn> create-net
  "Generates a new net."
  :- net-type
  [hooks :- hooks-type storage-factory :- (Fn [-> HookStorage])]
  (let [state (zipmap hooks (repeatedly storage-factory))]
    {:send (partial process hooks state)
     :check (partial check-hook state)}))

(defn> check-net
  :- (Map (Coll Any) Number)
  [net :- net-type hook :- hook-type]
  ((:check net) hook))

(defn> send-net
  :- Number
  [net :- net-type val :- input-type]
  ((:send net) val))
