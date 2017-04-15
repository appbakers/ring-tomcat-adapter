(ns ring.adapter.tomcat-test
  (:require [clojure.test :refer :all]
            [clj-http.client :as client]
            [ring.adapter.tomcat :as ring-tomcat]))

(defn- hello-world [req]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Hello, World"})

(defmacro with-server [app options & body]
  `(let [s# (ring-tomcat/run-tomcat ~app ~(assoc options :await? false))]
     (try
       ~@body
       (finally (.stop s#)))))

(deftest test-run-tomcat
  (testing "Tomcat HTTP"
    (with-server hello-world {:port 3000 :http? true}
      (let [response (client/get "http://localhost:3000/")]
        (is (= (:status response) 200))
        (is (= (:body response) "Hello, World")))))

  (testing "Tomcat HTTPS"
    (with-server hello-world {:port 3443 :https? true :keystore "resources/keystore.jks" :key-password "ring-tomcat-adapter"}
      (let [response (client/get "https://localhost:3443" {:insecure? true})]
        (is (= (:status response) 200))
        (is (= (:body response) "Hello, World"))))))
