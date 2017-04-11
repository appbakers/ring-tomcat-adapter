(ns ring.adapter.tomcat
  "Ring adapter for Apache Tomcat"
  (:import [org.apache.catalina.startup Tomcat])
  (:require [ring.util.servlet :as ring-servlet]))

(defn- create-server [options]
  (let [tomcat (doto (Tomcat.)
                 (.setPort (:port options 8080))
                 (.setBaseDir "."))
        server (.getServer tomcat)
        host (.getHost tomcat)]
    (.setAppBase host "resources")
    tomcat))

(defn run-tomcat
  "Start a Tomcat to serve given handler with supplied options:

  :port - the port to listen on (default: 8080)"
  [handler options]
  (let [server (create-server options)
        context (.addContext server "" ".")]
    (.addServlet server "" "ring-servlet" (ring-servlet/servlet handler))
    (.addServletMapping context "/*" "ring-servlet")
    (.start server)
    (.await (.getServer server))
    server))
