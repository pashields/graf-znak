(defproject graf-znak "0.0.1-SNAPSHOT"
  :description "A library for aggregating collections maps across multiple keys."
  :url "http://github.com/pashields/graf-znak"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/core.typed "0.2.26"]]
  :core.typed {:check [graf-znak.core
                       graf-znak.hook-storage
                       graf-znak.atom-storage
                       graf-znak.concurrent-hash-storage]})
