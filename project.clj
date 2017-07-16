(defproject caller-id "0.1.0-SNAPSHOT"
  :description "Caller ID Task Solution"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [metosin/compojure-api "1.1.10"]
                 [ring/ring-jetty-adapter "1.6.1"]
                 [org.clojure/tools.cli "0.3.5"]
                 [conman "0.6.3"]
                 [funcool/cats "2.1.0"]
                 [mount "0.1.11"]
                 [com.h2database/h2 "1.4.195"]
                 [org.clojure/java.jdbc "0.6.1"]
                 [com.layerware/hugsql "0.4.7"]
                 [com.fzakaria/slf4j-timbre "0.3.5"]
                 [com.googlecode.libphonenumber/libphonenumber "8.4.3"]
                 [com.taoensso/timbre "4.10.0"]
                 [org.clojure/data.csv "0.1.4"]
                 [semantic-csv "0.2.0"]]
  :ring {:handler caller-id.handler.app}
  :main caller-id.core
  :resource-paths ["resources"]
  :profiles {:uberjar {:aot :all
                       :uberjar-name "caller_id.jar"}
             :dev {:dependencies [[javax.servlet/javax.servlet-api "3.1.0"]
                                  [cheshire "5.6.3"]
                                  [org.clojure/tools.nrepl "0.2.13"]]
                   :plugins [[lein-ring "0.10.0"]]}
             :test {:dependencies [[ring/ring-mock "0.3.0"]
                                   [cheshire "5.7.1"]
                                   [ring/ring-mock "0.3.0"]
                                   [org.clojure/tools.nrepl "0.2.13"]]
                    :plugins [[lein-ring "0.10.0"]]}})
