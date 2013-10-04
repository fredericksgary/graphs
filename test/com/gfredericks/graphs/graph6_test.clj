(ns com.gfredericks.graphs.graph6-test
  (:require [com.gfredericks.graphs :as g]
            [com.gfredericks.graphs.graph6 :refer :all]
            [simple-check.clojure-test :refer [defspec]]
            [simple-check.generators :as gen]
            [simple-check.properties :as prop]))


(def gen-graph
  [:gen (fn [^java.util.Random r size]
          (let [order (-> (gen/call-gen gen/pos-int r size)
                          (Math/sqrt)
                          (int)
                          (* 5))
                p (.nextDouble r)]
            (g/rand-graph order p r)))])


(defspec graph6-format-works-correctly
  200
  (prop/for-all [g gen-graph]
    (let [s (->graph6 g)
          g' (graph6->vector-graph s)]
      (and (every? #(<= 0 % 127) (map int s))
           (= (g/order g) (g/order g'))
           (= (set (map set (g/edges g)))
              (set (map set (g/edges g'))))))))
