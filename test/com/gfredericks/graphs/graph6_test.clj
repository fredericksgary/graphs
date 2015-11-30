(ns com.gfredericks.graphs.graph6-test
  (:require [com.gfredericks.graphs :as g]
            [com.gfredericks.graphs.graph6 :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]))


(defn gen-graph*
  [order]
  (gen/let [flags (gen/vector gen/boolean (/ (* order (dec order)) 2))]
    (apply g/vector-graph order
           (->> (for [x (range order)
                      y (range x)]
                  [x y])
                (map vector flags)
                (filter first)
                (map second)))))

(def gen-graph
  (gen/bind (gen/choose 0 10) gen-graph*))

(defn remove-vertex
  [g v]
  {:pre [(pos? (g/order g))]}
  (apply g/vector-graph
         (dec (g/order g))
         (for [[a b] (g/edges g)
               :when (distinct? a b v)
               :let [a' (cond-> a (> a v) (dec))
                     b' (cond-> b (> b v) (dec))]]
           [a' b'])))

(defspec graph6-format-works-correctly
  200
  (prop/for-all [g gen-graph]
    (let [s (->graph6 g)
          g' (graph6->vector-graph s)]
      (and (every? #(<= 0 % 127) (map int s))
           (= (g/order g) (g/order g'))
           (= (set (map set (g/edges g)))
              (set (map set (g/edges g'))))))))
