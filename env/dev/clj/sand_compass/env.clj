(ns sand-compass.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [sand-compass.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[sand-compass started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[sand-compass has shut down successfully]=-"))
   :middleware wrap-dev})
