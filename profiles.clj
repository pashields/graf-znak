{:dev
 {:global-vars {*warn-on-reflection* true
                *assert* true}
  :dependencies [[org.clojure/test.check "0.5.7"]
                 [criterium "0.4.3"]
                 [org.clojure/data.csv "0.1.2"]]
  :plugins [[lein-typed "0.3.3"]]}
 :provided
 {:global-vars {*assert* false}}}
