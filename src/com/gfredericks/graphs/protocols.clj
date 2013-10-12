(ns com.gfredericks.graphs.protocols
  (:refer-clojure :exclude [empty]))

;; TODO: what should be the API for adding/removing vertices,
;; especially considering the (range n) restriction for vertexes?
;;
;; A minimal attempt would be (pop-vertex g) and (add-vertex g) where
;; the vertex to add/remove is assumed to be (dec n) or n
;; respectively. Wanting to remove any other vertex would require a
;; relabeling.

(defprotocol IGraph
  (order [g])
  (neighbors [g v])
  (edges [_] "Returns a list of vertex pairs. Should be considered unordered.")
  (vertices [g])
  (edge? [g e])
  (add-edge [g e])
  (remove-edge [g e])
  (empty [g]))
