(ns sand-compass.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[sand-compass started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[sand-compass has shut down successfully]=-"))
   :middleware identity})
