# ring-tomcat-adapter

[Tomcat](https://tomcat.apache.org/) adapter for [Ring](https://github.com/ring-clojure/ring).

## Install

To use tomcat as ring adapter, add following in your project dependencies.

```
[marchrock/ring-tomcat-adapter "0.4.3"]
```

ring-tomcat-adapter is available from [Clojars](https://clojars.org/marchrock/ring-tomcat-adapter)


## Usage

Tomcat adapter functionality is provided from namespace `ring.adapter.tomcat`.
```
;; In REPL
(require '[ring.adapter.tomcat :as tomcat])

;; In application
(ns example-app.core
  (:require [ring.adapter.tomcat :as tomcat]))
```

Then run you ring handler with Tomcat.
```
;; Basic ring handler as example
(defn handler [req]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Hello, World!\n"})

;; Boot Tomcat
(run-tomcat handler {:port 8080})
```


## License

Copyright © 2017 marchrock

Distributed under the Apache License, version 2.0.
