(ns com.gfredericks.graphs.graph6
  "Functions for converting to and from the graph6 ASCII format.

   Spec is here: http://cs.anu.edu.au/~bdm/data/formats.txt"
  (:require [com.gfredericks.graphs.protocols :as g]
            [com.gfredericks.graphs.impl :refer [vector-graph]]))

;; TODO: sparse6?

(defn ^:private prefix-length
  [order]
  (cond (<= 0 order 62) 1
        (<= 63 order 258047) 4
        (<= 258048 order 68719476735) 8
        :else (throw (ex-info "Bad order!" {:order order}))))

(defn ^:private read-order
  [s]
  (let [[x :as xs] (map #(- (int %) 63) s)]
    (cond (< x 63)
          x

          (< (second xs) 63)
          (let [[_ a b c] xs]
            (+ c (* b 64) (* a 4096)))

          :else
          (->> xs
               (drop 2)
               (take 6)
               (map * [1073741824 16777216 262144 4096 64 1])
               (reduce +)))))

(defn ^:private write-order
  "Returns a string which is the graph6 prefix for the given order."
  [order]
  (letfn [(s [nums]
            (->> nums (map #(char (+ 63 %))) (apply str)))
          (field [n i] (bit-and 63 (bit-shift-right n (* i 6))))]
    (cond (<= 0 order 62) (s [order])
          (<= 63 order 258047) (s [63 (field order 2) (field order 1) (field order 0)])
          (<= 258048 order 68719476735) (s (list* 63 63 (map #(field order %) (range 5 -1 -1)))))))

(defn ^:private edge-ordering
  [order]
  (for [a (range order)
        b (range a)]
    [b a]))

(defn ^:private read-edges
  [order s]
  (->> s
       (drop (prefix-length order))
       (mapcat (fn [es c]
                 (let [n (-> c int (- 63))]
                   (->> es
                        (map (fn [i e]
                               (if (bit-test n i) e))
                             [5 4 3 2 1 0])
                        (remove nil?))))
               (partition-all 6 (edge-ordering order)))))

(defn graph6->vector-graph
  [s]
  (let [order (read-order s)]
    (apply vector-graph order (read-edges order s))))

(defn ->graph6
  [g]
  (let [order (g/order g)]
    (->> (edge-ordering order)
         (partition-all 6)
         (map (fn [edges]
                (as-> edges <>
                      (map list <> [5 4 3 2 1 0])
                      (reduce (fn [n [e i]]
                                (cond-> n
                                        (g/edge? g e)
                                        (bit-set i)))
                              0
                              <>)
                      (+ 63 <>)
                      (char <>))))
         (apply str (write-order order)))))

;;;;;;;;;;;;;;;;;;;
;;               ;;
;; Serialization ;;
;;               ;;
;;;;;;;;;;;;;;;;;;;

(defmethod print-method com.gfredericks.graphs.impl.VectorGraph
  [g ^java.io.Writer w]
  (doto w
    (.write "#graphs/graph \"")
    (.write (->graph6 g))
    (.write "\"")))
