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

(def big-graph
  (rand-graph 100 0.1 (Random. 42)))

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

(bench (distances-per-node "IzBd`PwMo"))

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
