(defproject ring-tomcat-adapter :lein-v
  :description "Ring Apache Tomcat adapter"
  :url "http://github.com/marchrock/ring-tomcat-adapter"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :release-tasks [["vcs" "assert-committed"]
                  ["v" "update"]
                  ["vcs" "push"]
                  ["deploy" "clojars"]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [ring/ring-core "1.6.0-RC2"]
                 [ring/ring-servlet "1.6.0-RC2"]
                 [org.apache.tomcat.embed/tomcat-embed-core "8.5.13"]])
