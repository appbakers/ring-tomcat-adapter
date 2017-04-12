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
    (with-server hello-world {:port 3000}
      (let [response (client/get "http://localhost:3000/")]
        (is (= (:status response) 200))
        (is (= (:body response) "Hello, World"))))))
