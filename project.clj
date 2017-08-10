(defproject marchrock/ring-tomcat-adapter "0.4.4"
  :description "Ring Apache Tomcat adapter"
  :url "http://github.com/marchrock/ring-tomcat-adapter"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [ring/ring-core "1.6.2"]
                 [ring/ring-servlet "1.6.2"]
                 [org.apache.tomcat.embed/tomcat-embed-core "8.5.20"]]

  :profiles
  {:dev {:dependencies [[clj-http "3.5.0"]]}
   :uberjar {:omit-source true
             :aot :all
             :uberjar-name "ring-tomcat-adapter.jar"
             :source-paths ["env/prod/clj"]
             :resource-paths ["env/prod/resources"]}})
