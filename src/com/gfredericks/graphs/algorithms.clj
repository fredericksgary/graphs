(ns com.gfredericks.graphs.algorithms
  (:refer-clojure :exclude [empty])
  (:require [clojure.math.combinatorics :as comb]
            [com.gfredericks.graphs :refer :all]))


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
    [ds node]
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
    (reduce distances-through init (vertices g))))

(let [map-from-fn (fn [f ks] (into {} (map (juxt identity f) ks)))]
  (defn distances-per-node
    "Returns a map from each node to the sum of its
     distances to all other nodes."
    [g]
    (let [apd (all-pairs-distances g)]
      (map-from-fn
       (fn [node] (apply + (map apd (filter #(% node) (keys apd)))))
       (vertices g)))))

(defn shortest-path
  "Returns a sequence of vertices starting with `start` and ending
  with `end`, or nil if the two vertices are not connected."
  [g start end]
  (loop [shortest-paths-to {start [start]}
         next-gen [start]]
    (or (shortest-paths-to end)
        (let [paths (for [v next-gen
                          neighbor (neighbors g v)
                          :when (not (shortest-paths-to neighbor))]
                      (conj (shortest-paths-to v) neighbor))]
          (recur (into shortest-paths-to
                       (for [path paths]
                         [(peek path) path]))
                 (map peek paths))))))

(defn distance
  "Returns an integer distance, or nil if the vertices are not in the
  same connected component"
  [g v1 v2]
  (some-> (shortest-path g v1 v2) count))

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

(defn connected?
  [g]
  (loop [current #{0}
         seen #{}]
    (let [next (->> current
                    (mapcat #(neighbors g %))
                    (distinct)
                    (remove current)
                    (remove seen)
                    (set))]
      (if (empty? next)
        (= (order g) (+ (count current) (count seen)))
        (recur next (into seen current))))))

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

; assumes that the vertices are (range (:order g))
(defn isomorphisms
  "Returns a lazy seq of all permutations that transform g1 into g2."
  ([g1 g2]
     (isomorphisms (->> (edges g1)
                        (map set)
                        (set))
                   (->> (edges g2)
                        (map set)
                        (set))
                   []
                   (-> g1 order range set)))
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
