(ns reschedul2.middleware.authenticated
  (:require [buddy.auth :refer [authenticated?]]
            [ring.util.http-response :refer [unauthorized]]
            [taoensso.timbre :as timbre]))

(defn authenticated-mw
  "Middleware used in routes that require authentication. If request is not
   authenticated a 401 not authorized response will be returned"
  [handler]
  (fn [request]
    ; (timbre/warn (str "\n\n\n\n\nAUTH???:" (authenticated? request) "\n\n\n\n
    ; n"))
    (if (authenticated? request)
      ; (do
        ; (timbre/warn (str "req: " request "\n\n\n"))
      (handler request)
      (unauthorized {:error "Not authorized"}))))
