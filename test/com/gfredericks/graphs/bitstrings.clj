(ns nf.test.graphs.bitstrings
  (:refer-clojure :exclude [nth count assoc seq])
  (:use clojure.test
        nf.graphs.bitstrings))

(deftest strings-test
  (is (= (repeat 6 0) (seq "?")))
  (is (= [0 0 0 0 1 0, 0 0 0 0 1 1, 0 0 0 1 0 0] (seq "ABC")))

  (are [i b] (= b (nth "ABC" i))
       0 0
       1 0
       4 1
       5 0
       17 0
       15 1)

  (is (= "ARC" (assoc "ABC" 7 1)))
  (is (= "ABC" (assoc "ARC" 7 0)))
  (is (= "ARC" (assoc "ARC" 7 1)))
  )