(defproject com.gfredericks/graphs "0.3.6-SNAPSHOT"
  :description "Undirected graph-theory clojure stuff."
  :url "https://github.com/fredericksgary/graphs"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/math.combinatorics "0.0.4"]
                 [potemkin "0.3.3"]]
  :deploy-repositories [["releases" :clojars]]
  :profiles {:dev {:dependencies [[criterium "0.4.2"]
                                  [org.clojure/test.check "0.9.0"]]}})
