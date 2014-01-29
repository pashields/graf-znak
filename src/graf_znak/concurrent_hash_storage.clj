(ns graf-znak.concurrent-hash-storage
  (:require [clojure.core.typed :refer :all]
            [graf-znak.hook-storage :refer :all])
  (:import [java.util.concurrent.atomic AtomicLong]
           [java.util.concurrent ConcurrentHashMap]))

(ann-datatype ConcurrentHashStorage [storage :- ConcurrentHashMap])
(deftype ConcurrentHashStorage [^ConcurrentHashMap storage]
  HookStorage
  (inc-hook
    [this group]
    (.putIfAbsent storage group (AtomicLong.))
    (let [^AtomicLong count (.get storage group)]
      (.incrementAndGet count)))
  (get-groups
    [this]
    (let [keys (.keySet storage)
          pairs (map (fn [key] [key (.longValue ^AtomicLong (.get storage key))]) keys)]
      (into {} pairs))))

(defn> factory
  :- ConcurrentHashStorage
  []
  (ConcurrentHashStorage. (ConcurrentHashMap.)))
