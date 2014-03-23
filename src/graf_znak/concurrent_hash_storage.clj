(ns graf-znak.concurrent-hash-storage
  "Exposes the ConcurrentHashStorage type which implements HookStorage backed
   by a java ConcurrentHashMap.

   Accumulations done on this storage are not transactional. They are expected
   to mutate the existing state (specified originally by the accumulators
   initial state). They accumulators should be thread safe.

   Generally, this storage is only advisable if performance requires it."
  (:require [clojure.core.typed :refer :all]
            [graf-znak.hooks :refer :all]
            [graf-znak.accumulators :refer :all])
  (:import [java.util.concurrent ConcurrentHashMap]
           [java.util Map$Entry]
           [clojure.lang MapEntry]))

(non-nil-return java.util.concurrent.ConcurrentHashMap/contains :all)

;; A couple of no-checks here. Haven't figured a way to do these that isn't
;; awful quite yet. Punting for now.
(ann ^:no-check safe-put (All [x] (Fn [ConcurrentHashMap Object (Fn [-> x]) -> x])))
(defn safe-put
  "Like put if absent, but only actually loads the potential value after an
   initial empty check."
  [^ConcurrentHashMap hashmap key val-factory]
  (when (not (.contains hashmap key))
    (let [v (val-factory)]
      (assert (instance? Object v))
      (.putIfAbsent hashmap key v)))
  (let [v (.get hashmap key)]
    (assert v)
    v))

(ann ^:no-check convert-groups (Fn [ConcurrentHashMap -> (Map Object Object)]))
(defn convert-groups
  "Brings hashmap backed groups into clojure data structures."
  [^ConcurrentHashMap storage]
  (into {}
        (map (fn>
              :- (Vector* group-type (Map accumulator-name-type
                                          accumulator-state-type))
              [[g accums] :- MapEntry] [g (into {} accums)])
             storage)))

(ann-datatype ConcurrentHashStorage [storage :- ConcurrentHashMap])
(deftype ConcurrentHashStorage [^ConcurrentHashMap storage]
  HookStorage
  (accumulate-hook
    [this group record accumulator]
    (let [{accum-name :name init :init-fn accum-fn :fn} accumulator
          group-storage (safe-put storage group #(ConcurrentHashMap.))
          _ (assert (instance? ConcurrentHashMap group-storage))
          accum-state (safe-put group-storage accum-name init)]
      (accum-fn accum-state record)))
  (get-groups
    [this]
    (let [converted (convert-groups storage)]
      ;; As of this writing pred requires fully qualified symbols if check-ns
      ;; is called from another namespace. blech.
      (assert ((pred graf-znak.hooks/hook-result-type) converted))
      converted)))

(defn> factory
  :- ConcurrentHashStorage
  []
  (ConcurrentHashStorage. (ConcurrentHashMap.)))
