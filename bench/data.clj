(ns graf-znak.data
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]))

(defn csv-to-maps
  [csv-lines]
  (let [col-names (first csv-lines)
        records (rest csv-lines)]
    (map (partial zipmap col-names) records)))

(defn- run
  [bench-fn file hooks]
  (with-open [in (io/reader file)]
    (let [raw-csv (csv/read-csv in)
          records (csv-to-maps raw-csv)]
      (doall records)
      (bench-fn records hooks))))

(defn run-retailers
  [bench-fn]
  (run bench-fn
       "bench/data/Lower_Manhattan_Retailers.csv"
       [["Block-Lot"]
        ["CnAdrPrf_ZIP" "Primary" "Secondary"]
        ["CnAdrPrf_Addrline1"]
        ["CnBio_Org_Name"]]))

(defn run-local-48
  [bench-fn]
  (run bench-fn
       "bench/data/Local_Law_48_Of_2011_Report.csv"
       [["Parcel Name"]
        ["Boro" "Map Atlas" "Block" "Lot"]
        ["Agency"]
        ["Agency" "Boro"]
        ["ZipCode" "Structure Completed"]
        ["Boro" "Map Atlas" "Block" "Lot" "Agency" "ZipCode"]]))
