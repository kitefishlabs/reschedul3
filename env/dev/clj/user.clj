(ns user
  (:require [mount.core :as mount]
            [reschedul2.figwheel :refer [start-fw stop-fw cljs]]
            reschedul2.core))

(defn start []
  (mount/start-without #'reschedul2.core/http-server
                       #'reschedul2.core/repl-server))

(defn stop []
  (mount/stop-except #'reschedul2.core/http-server
                     #'reschedul2.core/repl-server))

(defn restart []
  (stop)
  (start))


