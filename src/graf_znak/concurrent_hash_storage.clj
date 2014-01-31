(ns graf-znak.concurrent-hash-storage
  (:require [clojure.core.typed :refer :all]
            [graf-znak.hook-storage :refer :all])
  (:import [java.util.concurrent.atomic AtomicLong]
           [java.util.concurrent ConcurrentHashMap]))

(ann ^:no-check clojure.core/iterator-seq (Fn [java.util.Iterator -> (Seq Any)]))
(non-nil-return java.util.concurrent.ConcurrentHashMap/keySet :all)

(ann-datatype ConcurrentHashStorage [storage :- ConcurrentHashMap])
(deftype ConcurrentHashStorage [^ConcurrentHashMap storage]
  HookStorage
  (inc-hook
    [this group]
    (.putIfAbsent storage group (AtomicLong.))
    (let [^AtomicLong count (.get storage group)]
      (assert (not (nil? count)))
      (assert (instance? AtomicLong count))
      (.incrementAndGet count)))
  (get-groups
    [this]
    (let [keys (.iterator (.keySet storage))
          get-longs (fn> 
                    :- (Coll (U (Coll Any) Long)) 
                    [key :- Any] 
                    (assert (coll? key))
                    (let [^AtomicLong count (.get storage key)]
                      (assert (not (nil? count)))
                      (assert (instance? AtomicLong count))
                      [key (.longValue count)]))
          _ (assert (not (nil? keys)))
          keys-seq (iterator-seq keys)]
      (into {} (map get-longs keys-seq)))))

(defn> factory
  :- ConcurrentHashStorage
  []
  (ConcurrentHashStorage. (ConcurrentHashMap.)))
