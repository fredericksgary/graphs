(ns com.gfredericks.graphs.algorithms-test
  (:refer-clojure :exclude [empty])
  (:require [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [com.gfredericks.graphs :refer :all]
            [com.gfredericks.graphs.graph6-test :refer [gen-graph]]
            [com.gfredericks.graphs.algorithms :refer :all]))

(deftest graph-atts-test
  (let [g {:order 7, :edges #{#{0 2} #{1 3} #{2 3} #{1 4} #{2 4} #{0 5} #{3 5}
                              #{4 5} #{0 6} #{1 6} #{3 6} #{4 6} #{5 6}}}]
    (is (connected? g))
    (is (= (all-pairs-distances g)
           {#{0 1} 2,
            #{0 2} 1,
            #{0 3} 2,
            #{0 4} 2,
            #{0 5} 1,
            #{0 6} 1,
            #{1 2} 2,
            #{1 3} 1,
            #{1 4} 1,
            #{1 5} 2,
            #{1 6} 1,
            #{2 3} 1,
            #{2 4} 1,
            #{2 5} 2,
            #{2 6} 2,
            #{3 4} 2,
            #{3 5} 1,
            #{3 6} 1,
            #{4 5} 1,
            #{4 6} 1,
            #{5 6} 1}))
    (is (= (degrees g)
           {0 3, 1 3, 2 3, 3 4, 4 4, 5 4, 6 5}))
    ;; I _think_ it's three...
    (is (= 3 (edge-connectivity g)))))

(deftest edge-connectivity-test
  (are [ec g] (= ec (edge-connectivity g))
       0 {:order 3, :edges #{}}
       0 {:order 3, :edges #{#{1 2}}}
       1 {:order 3, :edges #{#{1 2} #{0 1}}}
       2 {:order 3, :edges #{#{0 1} #{1 2} #{0 2}}}
       2 {:order 4, :edges #{#{0 1} #{1 2} #{2 3} #{3 0}}}
       1 {:order 4, :edges #{#{0 1} #{2 3} #{3 0}}}
       3 {:order 4, :edges #{#{0 1} #{1 2} #{2 3} #{3 0} #{0 2} #{1 3}}}))

(deftest unfairness-test
  (let [g {:order 5, :edges #{#{0 4} #{2 3} #{1 4} #{2 4} #{3 4}}}]
    (is (= 3 (unfairness g)))))

(deftest automorphisms-test
  (let [g {:order 6, :edges #{#{0 3} #{0 4} #{1 2} #{2 5} #{2 4} #{3 4}}}]
    (is (= 4 (count (automorphisms g)))))
  (let [g {:order 9, :edges #{#{0 1} #{1 2} #{0 3} #{1 4} #{2 5} #{3 4}
                              #{4 5} #{3 6} #{4 7} #{5 8} #{6 7} #{7 8}}}]
    (is (= 8 (count (automorphisms g)))))
  ; a square
  (let [g {:order 4, :edges #{#{0 1} #{1 2} #{2 3} #{3 0}}}]
    (is (= (set (automorphisms g))
           #{[0 1 2 3] [1 2 3 0] [2 3 0 1] [3 0 1 2]
             [3 2 1 0] [2 1 0 3] [1 0 3 2] [0 3 2 1]}))))

(defspec automorphisms-spec
  (prop/for-all [g gen-graph]
    (every? #(= g (permute g %))
            (automorphisms g))))

(deftest isomorphisms-test
  (let [g1 {:order 10, :edges #{#{0 5} #{2 4} #{3 5} #{2 7} #{1 8} #{4 6}
                                #{3 7} #{1 9} #{4 7} #{3 8} #{6 7} #{5 8}
                                #{4 9} #{6 8} #{5 9} #{6 9} #{7 9} #{8 9}}},
        g2 {:order 10, :edges #{#{0 4} #{2 3} #{4 5} #{3 6} #{2 7} #{1 8}
                                #{3 7} #{1 9} #{5 7} #{4 8} #{3 9} #{6 7}
                                #{5 8} #{4 9} #{6 8} #{5 9} #{6 9} #{7 9}
                                #{8 9}}},
        g3 (add-edge g1 #{3 9}),
        isos (isomorphisms g2 g3)]
    (is (some #{[0 1 2 4 5 3 6 7 8 9]} isos))))

(deftest connected-components-test
  (let [set-set #(set (map set %))]
    (is (= (hash-set (set (range 8)))
           (set-set
            (connected-components
             {:order 8,
              :edges
              #{#{0 1} #{0 2} #{1 2} #{0 4} #{1 3} #{1 4} #{2 3} #{2 6} #{3 7}
                #{5 6} #{6 7}}}))))
    (is (= (hash-set (set (range 8)))
           (set-set
            (connected-components
             {:order 8,
              :edges
              #{#{0 1} #{0 2} #{0 3} #{1 2} #{0 4} #{1 3} #{0 5} #{2 3} #{0 6}
                #{1 5} #{2 4} #{0 7} #{3 4} #{1 7} #{2 6} #{3 5} #{2 7} #{4 5}
                #{3 7} #{4 6} #{5 7} #{6 7}}}))))
    (is (= #{#{0} #{1 6} #{2 3} #{4} #{5} #{7}}
           (set-set (connected-components {:order 8, :edges #{#{2 3} #{1 6}}}))))
    (is (= #{#{0 1 2 3 4 6 7} #{5}}
           (set-set (connected-components {:order 8, :edges #{#{0 2} #{0 4} #{1 3} #{0 7} #{3 7} #{4 6}}}))))
    (is (= #{#{0} #{1 6} #{2} #{3 4 5 7}}
           (set-set (connected-components {:order 8, :edges #{#{1 6} #{3 4} #{4 7} #{5 7}}}))))))

(defspec connected-vs-connected-components-test 100
  (prop/for-all [g gen-graph]
    (= (boolean (connected? g))
       (= 1 (count (connected-components g))))))
