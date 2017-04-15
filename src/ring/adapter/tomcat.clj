(ns ring.adapter.tomcat
  "Ring adapter for Apache Tomcat"
  (:import [org.apache.catalina.startup Tomcat]
           [org.apache.catalina.core JreMemoryLeakPreventionListener]
           [org.apache.catalina.connector Connector]
           [org.apache.coyote.http11 Http11NioProtocol]
           [org.apache.tomcat.util.net SSLHostConfig])
  (:require [ring.util.servlet :as ring-servlet]))

(def http-connector "org.apache.coyote.http11.Http11NioProtocol")

(def https-ciphers
  (str "ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384:"
       "ECDHE-ECDSA-CHACHA20-POLY1305:ECDHE-RSA-CHACHA20-POLY1305:"
       "ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:"
       "ECDHE-ECDSA-AES256-SHA384:ECDHE-RSA-AES256-SHA384:"
       "ECDHE-ECDSA-AES128-SHA256:ECDHE-RSA-AES128-SHA256"))

(defn- create-ssl-host-config [options]
  (let [ssl-host-config (SSLHostConfig.)]
    (doto ssl-host-config
      (.setHostName (:ssl-host-name options "_default_"))
      (.setCertificateKeystoreFile (:keystore options nil))
      (.setCertificateKeystorePassword (:key-password options nil))
      (.setCiphers https-ciphers)
      (.setSslProtocol (:ssl-protocol options "TLS")))
    ssl-host-config))

(defn- create-http-connector []
  (let [connector (doto (Connector. http-connector))]
    connector))

(defn- create-https-connector [options]
  (let [connector (create-http-connector)
        ssl-config (create-ssl-host-config options)]
    (doto connector
      (.setScheme "https")
      (.setSecure true)
      (.addSslHostConfig ssl-config))
    (.setSSLEnabled ^Http11NioProtocol (.getProtocolHandler connector) true)
    connector))

(defn- create-connector [options]
  (let [connector (cond
                    (:https? options false) (create-https-connector options)
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
