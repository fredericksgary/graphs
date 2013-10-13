(ns com.gfredericks.graphs.serialization-test
  (:require [com.gfredericks.graphs :as g]
            [com.gfredericks.graphs.graph6-test :refer [gen-graph]]
            [simple-check.clojure-test :refer [defspec]]
            [simple-check.generators :as gen]
            [simple-check.properties :as prop]))

(defspec serialization-roundtrip
  200
  (prop/for-all [g gen-graph]
    (= g (-> g pr-str read-string))))
