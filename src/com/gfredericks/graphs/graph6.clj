(ns nf.graph6
  (:require [clojure.set :as set]))

;; I think this namespace should be deprecated now that String implements IGraph

(defn graph6-to-set
  [g6]
  (let [[order & bits] (map #(- (int %) 63) g6),
        edges
          (loop [es #{}, a 1, b 0, bitnum 0, bs bits, shamt 5]
            (if (= order a)
              es
              (if (= a b)
                (recur es (inc a) 0 bitnum bs shamt)
                (let [new-es (if (pos? (bit-and (first bs) (bit-shift-left 1 shamt))) (conj es #{a b}) es),
                      [new-bs new-shamt]
                        (if (zero? shamt)
                          [(rest bs) 5]
                          [bs (dec shamt)])]
                  (recur new-es a (inc b) (inc bitnum) new-bs new-shamt)))))]
    {:order order, :edges edges}))

(defn set-to-graph6
  [l])
