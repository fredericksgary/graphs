(ns com.gfredericks.graphs.performance
  "Testing the performance of various implementations with criterium"
  (:refer-clojure :exclude [empty])
  (:require [com.gfredericks.graphs :refer :all]
            [criterium.core :refer :all])
  (:import java.util.Random))

;;
;; Reading graphs
;;

(def small-graph
  (rand-graph 10 0.5 (Random. 42)))
(def small-graph-g6 "IzBd`PwMo")
(def small-graph-v (reduce add-edge (vector-graph 10) (edges small-graph)))


(def big-graph
  (rand-graph 100 0.1 (Random. 42)))
(def big-graph-v (reduce add-edge (vector-graph 100) (edges big-graph)))

(bench (distances-per-node small-graph))

;; WARNING: Final GC required 5.8781066487493305 % of runtime
;; Evaluation count : 14640 in 60 samples of 244 calls.
;;              Execution time mean : 4.589204 ms
;;     Execution time std-deviation : 1.592500 ms
;;    Execution time lower quantile : 4.075643 ms ( 2.5%)
;;    Execution time upper quantile : 10.210793 ms (97.5%)
;;                    Overhead used : 13.677124 ns

;; Found 5 outliers in 60 samples (8.3333 %)
;; 	low-severe	 1 (1.6667 %)
;; 	low-mild	 4 (6.6667 %)
;;  Variance from outliers : 96.4646 % Variance is severely inflated by outliers

(bench (distances-per-node small-graph-g6))

;; WARNING: Final GC required 2.744421165614739 % of runtime
;; Evaluation count : 13560 in 60 samples of 226 calls.
;;              Execution time mean : 4.390288 ms
;;     Execution time std-deviation : 382.317875 µs
;;    Execution time lower quantile : 4.064869 ms ( 2.5%)
;;    Execution time upper quantile : 5.090925 ms (97.5%)
;;                    Overhead used : 14.041105 ns

;; Found 2 outliers in 60 samples (3.3333 %)
;; 	low-severe	 2 (3.3333 %)
;;  Variance from outliers : 63.5491 % Variance is severely inflated by outliers


(future
  (bench (distances-per-node big-graph)))


(bench (edge? small-graph [3 7]))
;; Evaluation count : 27541080 in 60 samples of 459018 calls.
;;              Execution time mean : 2.183497 µs
;;     Execution time std-deviation : 99.874263 ns
;;    Execution time lower quantile : 2.052883 µs ( 2.5%)
;;    Execution time upper quantile : 2.443489 µs (97.5%)
;;                    Overhead used : 14.041105 ns

;; Found 2 outliers in 60 samples (3.3333 %)
;; 	low-severe	 2 (3.3333 %)
;;  Variance from outliers : 31.9503 % Variance is moderately inflated by outliers

(bench (edge? "IzBd`PwMo" [3 7]))
;; Evaluation count : 40624980 in 60 samples of 677083 calls.
;;              Execution time mean : 1.764110 µs
;;     Execution time std-deviation : 962.008473 ns
;;    Execution time lower quantile : 1.464234 µs ( 2.5%)
;;    Execution time upper quantile : 4.193739 µs (97.5%)
;;                    Overhead used : 14.041105 ns

;; Found 3 outliers in 60 samples (5.0000 %)
;; 	low-severe	 3 (5.0000 %)
;;  Variance from outliers : 98.2487 % Variance is severely inflated by outliers


;;
;; Adding an edge
;;

(bench (add-edge small-graph [3 4]))
;; WARNING: Final GC required 2.794846475039227 % of runtime
;; Evaluation count : 12147540 in 60 samples of 202459 calls.
;;              Execution time mean : 5.155049 µs
;;     Execution time std-deviation : 309.206194 ns
;;    Execution time lower quantile : 4.738996 µs ( 2.5%)
;;    Execution time upper quantile : 5.745033 µs (97.5%)
;;                    Overhead used : 14.059235 ns

;; Found 1 outliers in 60 samples (1.6667 %)
;; 	low-severe	 1 (1.6667 %)
;;  Variance from outliers : 45.0903 % Variance is moderately inflated by outliers


(bench (add-edge small-graph-g6 [3 4]))
;; Evaluation count : 27111720 in 60 samples of 451862 calls.
;;              Execution time mean : 2.355260 µs
;;     Execution time std-deviation : 141.113890 ns
;;    Execution time lower quantile : 2.167621 µs ( 2.5%)
;;    Execution time upper quantile : 2.663957 µs (97.5%)
;;                    Overhead used : 14.059235 ns


(bench (add-edge small-graph-v [3 4]))
;; Evaluation count : 24525900 in 60 samples of 408765 calls.
;;              Execution time mean : 2.583545 µs
;;     Execution time std-deviation : 136.728794 ns
;;    Execution time lower quantile : 2.413202 µs ( 2.5%)
;;    Execution time upper quantile : 2.911257 µs (97.5%)
;;                    Overhead used : 14.059235 ns

;; Found 2 outliers in 60 samples (3.3333 %)
;; 	low-severe	 2 (3.3333 %)
;;  Variance from outliers : 38.5124 % Variance is moderately inflated by outliers


(bench (add-edge big-graph [7 8]))
;; Evaluation count : 16446840 in 60 samples of 274114 calls.
;;              Execution time mean : 3.746990 µs
;;     Execution time std-deviation : 163.516431 ns
;;    Execution time lower quantile : 3.535159 µs ( 2.5%)
;;    Execution time upper quantile : 4.164159 µs (97.5%)
;;                    Overhead used : 14.059235 ns

;; Found 4 outliers in 60 samples (6.6667 %)
;; 	low-severe	 4 (6.6667 %)
;;  Variance from outliers : 30.2954 % Variance is moderately inflated by outliers


(bench (add-edge big-graph-v [7 8]))
;; Evaluation count : 18827100 in 60 samples of 313785 calls.
;;              Execution time mean : 3.893665 µs
;;     Execution time std-deviation : 2.117835 µs
;;    Execution time lower quantile : 3.126893 µs ( 2.5%)
;;    Execution time upper quantile : 7.085311 µs (97.5%)
;;                    Overhead used : 14.059235 ns

;; Found 5 outliers in 60 samples (8.3333 %)
;; 	low-severe	 3 (5.0000 %)
;; 	low-mild	 2 (3.3333 %)
;;  Variance from outliers : 98.2482 % Variance is severely inflated by outliers
