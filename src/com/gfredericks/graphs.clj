(ns com.gfredericks.graphs
  "Functions for manipulation of undirected graphs. Since this is
   intended for study of the mathematical objects rather than
   graph-like data, the vertex set is restricted to (range n)."
  (:refer-clojure :exclude [empty])
  (:require [com.gfredericks.graphs.graph6]
            [com.gfredericks.graphs.impl]
            [com.gfredericks.graphs.protocols]
            [potemkin :refer [import-vars]]))

(import-vars
 [com.gfredericks.graphs.protocols
  order neighbors edges vertices edge? add-edge remove-edge empty]
 [com.gfredericks.graphs.impl
  vector-graph])

(defn rand-graph
  "Returns a random graph of the given order where each edge exists
   with probability p (default 0.5)."
  ([n] (rand-graph n 0.5))
  ([n p] (rand-graph n p (java.util.Random.)))
  ([n p ^java.util.Random r]
     (apply vector-graph n
            (for [x (range n),
                  y (range n),
                  :when (< x y),
                  :when (< (.nextDouble r) p)] [x y]))))
