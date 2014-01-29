(defproject graf-znak "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/core.typed "0.2.19"]
                 [org.clojure/algo.generic "0.1.1"]]
  :core.typed {:check [graf-znak.core
                       graf-znak.hook-storage
                       graf-znak.atom-storage]})
