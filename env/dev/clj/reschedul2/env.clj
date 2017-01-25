(ns reschedul2.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [reschedul2.config :refer [env]]
            [reschedul2.dev-middleware :refer [wrap-dev]]
            [reschedul2.db.core :refer [seed-database!]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[reschedul2 started successfully using the development profile]=-\n\n"))
    ;  (if (:reset-users env)
      ; (do)))
        ; (seed-database!)
        ; (log/info "\n-=[deleted users table]=-\n\n")
        ; (log/info "\n-=[created default admin user]=-\n\n"))))
   :stop
   (fn []
     (log/info "\n-=[reschedul2 has shut down successfully]=-"))
   :middleware wrap-dev})
