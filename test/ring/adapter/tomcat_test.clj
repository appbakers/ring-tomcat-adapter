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

  (testing "Tomcat HTTP and HTTPS"
    (with-server hello-world {:https-port 3443 :https? true :keystore "resources/keystore.jks" :key-password "ring-tomcat-adapter"}
      (let [response (client/get "https://localhost:3443" {:insecure? true})]
        (is (= (:status response) 200))
        (is (= (:body response) "Hello, World")))
      (let [response (client/get "http://localhost:8080" {:insecure? true})]
        (is (= (:status response) 200))
        (is (= (:body response) "Hello, World"))))))
