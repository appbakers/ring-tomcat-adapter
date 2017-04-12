(defproject marchrock/ring-tomcat-adapter "0.1.5-SNAPSHOT"
  :description "Ring Apache Tomcat adapter"
  :url "http://github.com/marchrock/ring-tomcat-adapter"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [ring/ring-core "1.6.0-RC2"]
                 [ring/ring-servlet "1.6.0-RC2"]
                 [org.apache.tomcat.embed/tomcat-embed-core "8.5.13"]
                 [clj-http "3.4.1"]])
