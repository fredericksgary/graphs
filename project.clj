(defproject com.gfredericks/graphs "0.3.1"
  :description "Undirected graph-theory clojure stuff."
  :url "https://github.com/fredericksgary/graphs"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/math.combinatorics "0.0.4"]
                 [potemkin "0.3.3"]]
  :profiles {:dev {:dependencies [[criterium "0.4.2"]
                                  [reiddraper/simple-check "0.4.1"]]}})
