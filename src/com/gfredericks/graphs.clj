(ns com.gfredericks.graphs
  "Functions for manipulation of undirected graphs. Since this is
   intended for study of the mathematical objects rather than
   graph-like data, the vertex set is restricted to (range n)."
  (:refer-clojure :exclude [empty])
  (:require [clojure.math.combinatorics :as comb]
            [com.gfredericks.graphs.bitstrings :as bits]))

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

(defn- g6-bitstring-index
  "Given two vertex indices returns the bit-index (including the six-bit
  order field) of the edge within the graph6 bitstring."
  [a b]
  (let [[a b] (if (< b a) [b a] [a b])
        base (/ (* b (dec b)) 2)]
    (+ base a 6)))

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
  (empty [this] (assoc this :edges #{}))

  ;; graph6 representation
  String
  (order [g6] (-> g6 first int (- 63)))
  (neighbors [g6 v]
    (for [a (range (order g6))
          :when (not= a v)
          :when (edge? g6 [a v])]
      a))
  (edge? [g6 e]
    (let [[a b] (seq e)]
      (= 1 (bits/nth g6 (g6-bitstring-index a b)))))
  (edges [g6]
    (for [a (range (order g6))
          b (range a)
          :when (edge? g6 [a b])]
      [a b]))
  (vertices [g6]
    (range (order g6)))
  (add-edge [g6 e]
    (let [[a b] (seq e)]
      (bits/assoc g6
                  (g6-bitstring-index a b)
                  1)))
  (remove-edge [g6 e]
    (let [[a b] (seq e)]
      (bits/assoc g6
                  (g6-bitstring-index a b)
                  0)))
  (empty [g6] (let [[c & cs] g6] (apply str c (repeat (count cs) \?)))))

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
  [order]
  (let [edge-count (/ (* order (dec order)) 2)
        long-count (cond-> (quot edge-count 64)
                           (pos? (rem edge-count 64)) (inc))]
    (VectorGraph. order (vec (repeat long-count 0)))))


(defn all-pairs
  [order]
  (mapcat
    (fn [b]
      (map #(hash-set % b) (range b)))
    (range order)))

;; define a couple functions that handle :infinity
(let [+' #(if (some #{:infinity} [%1 %2])
           :infinity
           (+ %1 %2))
      min' #(cond (= :infinity %1) %2
                  (= :infinity %2) %1
                  :else (min %1 %2))]
  (defn distances-through
    [node ds]
    (into {}
          (map
           (fn [[pair distance]]
             (if (pair node)
               [pair distance]
               (let [[a b] (seq pair)]
                 [pair (min' distance (+' (ds #{a node}) (ds #{b node})))])))
           ds))))

(defn all-pairs-distances
  [g]
  (let [ap (all-pairs (order g))
        init (into {} (map #(if (edge? g %) [% 1] [% :infinity]) ap))]
    (loop [[node & nodes] (vertices g) ds init]
      (let [new-ds (distances-through node ds)]
        (if (empty? nodes)
          new-ds
          (recur nodes new-ds))))))

(let [map-from-fn (fn [f ks] (into {} (map (juxt identity f) ks)))]
  (defn distances-per-node
    "Returns a map from each node to the sum of its
     distances to all other nodes."
    [g]
    (let [apd (all-pairs-distances g)]
      (map-from-fn
       (fn [node] (apply + (map apd (filter #(% node) (keys apd)))))
       (vertices g)))))

(defn connected-components
  [g]
  (let [vertices (range (order g))
        groups (zipmap vertices (map list vertices))
        membership (vec vertices)]
    (->>
     (edges g)
     (reduce (fn [[groups membership :as acc] edge]
               (let [[a b] edge
                     ma (membership a)
                     mb (membership b)]
                 (if (= ma mb)
                   acc
                   ;; merge everybody in b into a
                   (let [groups* (-> groups
                                     (dissoc mb)
                                     (update-in [ma] concat (groups mb)))
                         membership* (reduce (fn [v i] (assoc v i ma)) membership (groups mb))]
                     [groups* membership*]))))
             [groups membership])
     first
     vals)))

(def connected? (comp #(= 1 %) count connected-components))

(defn permute
  "Given a graph and a permutation (which is some sort
   of (vec (shuffle (range order)))), returns a new graph with the
   vertices permuted according to the permutation."
  [g p]
  (reduce add-edge (empty g) (for [[a b] (edges g)] [(p a) (p b)])))

(defn induced-subgraph
  "g is a graph, coll is a collection of vertices. Returns the induced subgraph with vertices
   labeled 0 to n-1 (as normal) corresponding to the supplied vertices in sorted order."
  [g coll]
  (let [vs (set coll),
        edge-map (into {} (map vector (sort vs) (range (count vs))))]
    {:order (count vs),
     :edges
       (set
         (map
           (fn [edge]
             (let [[a b] (seq edge)]
               #{(edge-map a) (edge-map b)}))
           (filter
             (fn [edge]
               (let [[a b] (seq edge)]
                 (and (vs a) (vs b))))
             (:edges g))))}))

(defn disconnected-subgraphs
  [g order]
  (let [subgraphs (map
                    (partial induced-subgraph g)
                    (comb/combinations (range (:order g)) order))]
    (filter (comp not connected?) subgraphs)))

(defn connectivity
  "Measures vertex-connectivity."
  [g]
  (loop [k 0]
    (if (= k (dec (:order g)))
      k
      (if (empty? (disconnected-subgraphs g (- (:order g) k)))
        (recur (inc k))
        k))))

(defn edge-connectivity
  [g]
  (let [edges (vec (:edges g))
        edge-subgraphs (fn [num-edges-to-remove]
                         (for [edges-to-remove (comb/combinations edges num-edges-to-remove)]
                           (apply update-in g [:edges] disj edges-to-remove)))]
    (loop [k 0]
      (if (some (complement connected?) (edge-subgraphs k))
        k
        (recur (inc k))))))

(defn degrees
  "Given a graph, returns a map from vertex numbers to their degree."
  [g]
  ;; whoops this doesn't give zero on nonexist
  (->> (edges g)
       (apply concat)
       (frequencies)
       (merge (zipmap (vertices g) (repeat 0)))))

(defn unfairness
  [g]
  (let [ds (vals (distances-per-node g))]
    (- (apply max ds) (apply min ds))))

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

; assumes that the vertices are (range (:order g))
(defn isomorphisms
  "Returns a lazy seq of all permutations that transform g1 into g2."
  ([g1 {:keys [order edges]}]
    (isomorphisms (:edges g1) edges [] (set (range order))))
  ([g1-edges g2-edges mapping tos-left]
    (if (empty? tos-left)
      [mapping]
      (let [from (count mapping),
            compatible-tos
              (filter
                (fn [to]
                  (every?
                    (fn [other-from]
                      (=
                        (boolean (g1-edges #{from other-from}))
                        (boolean (g2-edges #{to (mapping other-from)}))))
                    (range (count mapping))))
                tos-left)]
        (mapcat
         (fn [to]
           (lazy-seq (isomorphisms g1-edges g2-edges (conj mapping to) (disj tos-left to))))
         compatible-tos)))))

(defn isomorphic? [g1 g2] (boolean (first (isomorphisms g1 g2))))

(defn automorphisms [x] (isomorphisms x x))
