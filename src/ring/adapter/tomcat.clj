(ns ring.adapter.tomcat
  "Ring adapter for Apache Tomcat"
  (:import [org.apache.catalina.startup Tomcat]
           [org.apache.catalina.core JreMemoryLeakPreventionListener]
           [org.apache.catalina.connector Connector])
  (:require [ring.util.servlet :as ring-servlet]))

(def http-connector "org.apache.coyote.http11.Http11NioProtocol")

(defn- create-http-connector []
  (let [connector (doto (Connector. http-connector))]
    connector))

(defn- create-connector [options]
  (let [connector (cond
                    (:http? options true) (create-http-connector)
                    :else (create-http-connector))]
    (doto connector
      (.setPort (:port options 8080)))
    connector))

(defn- create-server [options]
  (let [tomcat (doto (Tomcat.)
                 (.setBaseDir ".")
                 (.setConnector (create-connector options)))
        server (.getServer tomcat)
        host (.getHost tomcat)]
    (.addLifecycleListener server (JreMemoryLeakPreventionListener.))
    (.setAppBase host "resources")
    tomcat))

(defn run-tomcat
  "Start a Tomcat to serve given handler with supplied options

  :await? - block the thread until server get shutdown command (default: true)
  :http? - create http connector (default: true)
  :port - the port to listen on (default: 8080)"
  [handler options]
  (let [server (create-server options)
        context (.addContext server "" ".")]
    (.addServlet server "" "ring-servlet" (ring-servlet/servlet handler))
    (.addServletMapping context "/*" "ring-servlet")
    (.start server)
    (when (:await? options true)
      (.await (.getServer server)))
    server))
