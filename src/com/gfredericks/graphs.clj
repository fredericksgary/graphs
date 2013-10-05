(ns com.gfredericks.graphs
  "Functions for manipulation of undirected graphs. Since this is
   intended for study of the mathematical objects rather than
   graph-like data, the vertex set is restricted to (range n)."
  (:refer-clojure :exclude [empty]))

;; TODO: what should be the API for adding/removing vertices,
;; especially considering the (range n) restriction for vertexes?
;;
;; A minimal attempt would be (pop-vertex g) and (add-vertex g) where
;; the vertex to add/remove is assumed to be (dec n) or n
;; respectively. Wanting to remove any other vertex would require a
;; relabeling.

(defprotocol IGraph
  (order [g])
  (neighbors [g v])
  (edges [_] "Returns a list of vertex pairs. Should be considered unordered.")
  (vertices [g])
  (edge? [g e])
  (add-edge [g e])
  (remove-edge [g e])
  (empty [g]))

(extend-protocol IGraph

  ;; The standard set-representation we've been using, of a map with
  ;; :order and :edges keys
  clojure.lang.APersistentMap
  (order [this] (:order this))
  (neighbors [this v]
    (for [pair (filter #(% v) (:edges this))
          :let [[a b] (seq pair)]]
      (if (= a v) b a)))
  (edge? [this e]
    (let [[v1 v2] (seq e)]
      (boolean ((:edges this) #{v1 v2}))))
  (edges [this] (map seq (:edges this)))
  (vertices [this] (range (:order this)))
  (add-edge [this e]
    (let [[v1 v2] (seq e)]
      (update-in this [:edges] conj #{v1 v2})))
  (remove-edge [this e]
    (let [[v1 v2] (seq e)]
      (update-in this [:edges] disj #{v1 v2})))
  (empty [this] (assoc this :edges #{})))

(defn ^:private indices-64
  "Given a pair of vertex numbers, returns a pair of indices indicating
   where to find the relevant bit in a collection of 64-bit integers. The
   first index is the index of the relevant Long, while the second is the
   index within the long."
  [a b]
  (let [n? (< b a)
        [a b] (if (< b a) [b a] [a b])
        i (+ a (/ (* b (dec b)) 2))]
    ((juxt quot rem) i 64)))

(deftype VectorGraph [order adjacency]
  IGraph
  (order [_] order)
  (neighbors [_ a]
    (for [b (concat (range 0 a)
                    (range (inc a) order))
          :let [[i1 i2] (indices-64 a b)]
          :when (bit-test (adjacency i1) i2)]
      b))
  (edge? [_ e]
    (let [[a b] (seq e)
          [i1 i2] (indices-64 a b)]
      (bit-test (adjacency i1) i2)))
  (edges [_]
    (for [a (range 1 order)
          b (range a)
          :let [[i1 i2] (indices-64 a b)]
          :when (bit-test (adjacency i1) i2)]
      [a b]))
  (vertices [_] (range order))
  (add-edge [_ e]
    (let [[a b] (seq e)
          [i1 i2] (indices-64 a b)
          adjacency' (update-in adjacency
                                [i1]
                                bit-set
                                i2)]
      (VectorGraph. order adjacency')))
  (remove-edge [_ e]
    (let [[a b] (seq e)
          [i1 i2] (indices-64 a b)
          adjacency' (update-in adjacency
                                [i1]
                                bit-clear
                                i2)]
      (VectorGraph. order adjacency')))
  (empty [_]
    (VectorGraph. order (vec (repeat (count adjacency) 0)))))

(defn vector-graph
  [order & edges]
  (let [edge-count (/ (* order (dec order)) 2)
        long-count (cond-> (quot edge-count 64)
                           (pos? (rem edge-count 64)) (inc))
        the-vector (vec (repeat long-count 0))

        the-vector (cond-> the-vector
                           (seq edges)
                           (as-> <>
                                 (transient <>)
                                 (reduce
                                  (fn [v e]
                                    (let [[a b] (seq e)
                                          [i j] (indices-64 a b)]
                                      (assoc! v i (bit-set (get v i) j))))
                                  <>
                                  edges)
                                 (persistent! <>)))]
    (VectorGraph. order the-vector)))

(defn rand-graph
  "Returns a random graph of the given order where each edge exists
   with probability p (default 0.5)."
  ([n] (rand-graph n 0.5))
  ([n p] (rand-graph n p (java.util.Random.)))
  ([n p ^java.util.Random r]
    {:order n,
     :edges (set (for [x (range n),
                       y (range n),
                       :when (< x y),
                       :when (< (.nextDouble r) p)] #{x y}))}))
