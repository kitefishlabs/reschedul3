(ns reschedul2.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [reschedul2.layout :refer [error-page]]
            [reschedul2.routes.home :refer [home-routes]]
            [reschedul2.routes.services :refer [service-routes]]
            [reschedul2.routes.oauth :refer [oauth-routes]]
            [compojure.route :as route]
            [reschedul2.env :refer [defaults]]
            [mount.core :as mount]
            [reschedul2.middleware :as middleware]))
            ; [reschedul2.db.core :as db]))

(mount/defstate init-app
                :start ((or (:init defaults) identity))
                :stop  ((or (:stop defaults) identity)))

(def app-routes
  (routes
    (-> #'home-routes
        (wrap-routes middleware/wrap-csrf)
        (wrap-routes middleware/wrap-formats))
    #'oauth-routes
    #'service-routes
    (route/not-found
      (:body
        (error-page {:status 404
                     :title "page not found"})))))


(defn app [] (middleware/wrap-base #'app-routes))
