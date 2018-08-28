(ns user
  (:require [sand-compass.config :refer [env]]
            [clojure.spec.alpha :as s]
            [expound.alpha :as expound]
            [mount.core :as mount]
            [sand-compass.figwheel :refer [start-fw stop-fw cljs]]
            [sand-compass.core :refer [start-app]]))

(alter-var-root #'s/*explain-out* (constantly expound/printer))

(defn start []
  (mount/start-without #'sand-compass.core/repl-server))

(defn stop []
  (mount/stop-except #'sand-compass.core/repl-server))

(defn restart []
  (stop)
  (start))


