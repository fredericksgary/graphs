(ns com.gfredericks.graphs.bitstrings-test
  (:require [clojure.test :refer :all]
            [com.gfredericks.graphs.bitstrings :as bits]))

(deftest strings-test
  (is (= (repeat 6 0) (bits/seq "?")))
  (is (= [0 0 0 0 1 0, 0 0 0 0 1 1, 0 0 0 1 0 0] (bits/seq "ABC")))

  (are [i b] (= b (bits/nth "ABC" i))
       0 0
       1 0
       4 1
       5 0
       17 0
       15 1)

  (is (= "ARC" (bits/assoc "ABC" 7 1)))
  (is (= "ABC" (bits/assoc "ARC" 7 0)))
  (is (= "ARC" (bits/assoc "ARC" 7 1))))