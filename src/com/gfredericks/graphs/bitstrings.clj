(ns com.gfredericks.graphs.bitstrings
  (:refer-clojure :exclude [count nth assoc seq])
  (:require [clojure.core :as core]))

(defprotocol IBitString
  (count [bs])
  (nth [bs i])
  (assoc [bs i b]))

;; Use the IBitString protocol to extend IGraph to both String (in graph6 format)
;; and Longs


(extend-protocol IBitString
  ;; graph6-style base64; only bitstrings with multiple-of-6 lengths are
  ;; representable
  String
  (count [g6]
    (* 6 (core/count g6)))
  (nth [g6 i]
    (let [char-index (quot i 6)
          char-rem6-index (rem i 6)
          n (-> g6 (.charAt char-index) int (- 63))]
      (-> n
          (bit-shift-right (- 5 char-rem6-index))
          (bit-and 1))))
  (assoc [g6 i b]
    (let [char-index (quot i 6)
          char-rem6-index (rem i 6)
          the-char (.charAt g6 char-index)
          bitnum (- (int the-char) 63)
          mask (bit-shift-right 32 char-rem6-index)
          setmask (if (pos? b) mask 0)
          clearmask (bit-not mask)
          bitnum (-> bitnum
                     (bit-and clearmask)
                     (bit-or setmask))]
      (str (subs g6 0 char-index)
           (-> bitnum (+ 63) char)
           (subs g6 (inc char-index))))))

(defn seq
  [bs]
  (for [i (range (count bs))]
    (nth bs i)))