;
; Copyright 2017 marchrock
;
; Licensed under the Apache License, Version 2.0 (the "License")
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;     http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.
;

(ns ring.adapter.tomcat
  "Ring adapter for Apache Tomcat"
  (:import [org.apache.catalina.startup Tomcat]
           [org.apache.catalina.core JreMemoryLeakPreventionListener StandardThreadExecutor]
           [org.apache.catalina.connector Connector]
           [org.apache.coyote AbstractProtocol]
           [org.apache.coyote.http11 Http11NioProtocol]
           [org.apache.tomcat.util.net SSLHostConfig]
           [org.apache.catalina Server Service])
  (:require [ring.util.servlet :as ring-servlet]))

(def default-http-port 8080)

(def default-https-port 8443)

(def http-connector "org.apache.coyote.http11.Http11NioProtocol")

(def https-ciphers
  (str "ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384:"
       "ECDHE-ECDSA-CHACHA20-POLY1305:ECDHE-RSA-CHACHA20-POLY1305:"
       "ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:"
       "ECDHE-ECDSA-AES256-SHA384:ECDHE-RSA-AES256-SHA384:"
       "ECDHE-ECDSA-AES128-SHA256:ECDHE-RSA-AES128-SHA256"))

(defn- create-executor [options]
  (let [executor (StandardThreadExecutor.)]
    (doto executor
      (.setName (:executor-name options "ring-executor"))
      (.setNamePrefix (str (:executor-name options "ring-executor") "-"))
      (.setMaxThreads (:max-threads options 200))
      (.setMinSpareThreads (:min-spare-threads options 25))
      (.setMaxIdleTime (:max-idle-time options 60000)))))

(defn- create-ssl-host-config [options]
  (let [ssl-host-config (SSLHostConfig.)]
    (doto ssl-host-config
      (.setHostName (:tls-hostname options "_default_"))
      (.setCertificateKeystoreFile (:keystore options nil))
      (.setCertificateKeystorePassword (:key-password options nil))
      (.setCiphers (:tls-ciphers options https-ciphers))
      (.setSslProtocol (:tls-protocol options "TLS")))
    ssl-host-config))

(defmacro create-executored-connector
  [connector-generator executor options]
  `(let [connector# (~connector-generator ~options)]
     (.setExecutor (cast AbstractProtocol (.getProtocolHandler connector#)) ~executor)
     connector#))

(defn- create-http-connector [options]
  (let [connector (Connector. http-connector)]
    (doto connector
      (.setPort (:port options default-http-port)))
    (when (:https? options false)
      (.setRedirectPort connector (:https-port options default-https-port)))
    connector))

(defn- create-https-connector
  [options]
  (let [connector (Connector. http-connector)
        ssl-config (create-ssl-host-config options)
        protocol-handler (.getProtocolHandler connector)]
    (doto connector
      (.setScheme "https")
      (.setSecure true)
      (.addSslHostConfig ssl-config)
      (.setPort (:https-port options default-https-port)))
    (.setSSLEnabled ^Http11NioProtocol (.getProtocolHandler connector) true)
    connector))

(defmacro create-executored-connectors
  [body ^Service service options]
  `(let [executor# (create-executor ~options)]
     (.addExecutor ~service executor#)
     (~body ~service ~options executor#)
     ~service))

(defmacro create-connector-fn
  [create-fn ^Service service options & executor]
  `(.addConnector
     ~service
     (if (:executor? ~options true)
       (create-executored-connector ~create-fn (first ~@executor) ~options)
       (~create-fn ~options))))

(defn- create-connector [^Service service options & executor]
  (when (:http? options true)
    (create-connector-fn create-http-connector service options executor))
  (when (:https? options false)
    (create-connector-fn create-https-connector service options executor))
  service)

(defn- create-server [options]
  (let [tomcat (doto (Tomcat.)
                 (.setBaseDir "."))
        server (.getServer tomcat)
        service (.getService tomcat)
        host (.getHost tomcat)]
    (if (:executor? options true)
      (create-executored-connectors create-connector service options)
      (create-connector service options))
    (.addLifecycleListener server (JreMemoryLeakPreventionListener.))
    (.setAppBase host "resources")
    tomcat))

(defn run-tomcat
  "Start a Tomcat to serve given handler with supplied options

  :await? - block the thread until server get shutdown command (default: true)
  :http? - create http connector (default: true)
  :port - the port to listen on http connector (default: 8080)
  :https? - create https connector (default: false)
  :https-port - the port to listen on https connector (default: 8443)
  :keystore - path to keystore file include server certificate
  :key-pass - password of keystore file
  :tls-hostname - hostname to listen for https connector (default: _default_)
  :tls-protocol - list of SSL/TLS protocol to support for https connector (default: TLS)
  :tls-ciphers - list of SSL/TLS ciphers to support for https connector (default: too long. ECDHE-ECDSA and ECDHE-RSA ciphers)"
  [handler options]
  (let [server (create-server options)
        context (.addContext server "" ".")]
    (.addServlet server "" "ring-servlet" (ring-servlet/servlet handler))
    (.addServletMapping context "/*" "ring-servlet")
    (.start server)
    (when (:await? options true)
      (.await ^Server (.getServer server)))
    server))
