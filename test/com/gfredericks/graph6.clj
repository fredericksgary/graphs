(ns nf.test.graph6
  (:use clojure.test)
  (:use nf.graph6))

(deftest basic-parse
  (is (= {:order 7, :edges #{#{0 2} #{1 3} #{2 3} #{1 4} #{2 4} #{0 5} #{3 5}
                             #{4 5} #{0 6} #{1 6} #{3 6} #{4 6} #{5 6}}}
         (graph6-to-set "FRY]w"))))
