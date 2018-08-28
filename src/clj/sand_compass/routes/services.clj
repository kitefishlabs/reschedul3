(ns sand-compass.routes.services
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [compojure.api.meta :refer [restructure-param]]
            [buddy.auth.accessrules :refer [restrict]]
            [buddy.auth :refer [authenticated?]]
            [sand-compass.routes.services.auth :refer :all]
            [sand-compass.routes.services.password :refer :all]
            [sand-compass.routes.services.permission :refer :all]
            [sand-compass.routes.services.preflight :refer :all]
            [sand-compass.routes.services.refresh-token :refer :all]
            [sand-compass.routes.services.user :refer :all]))

(defn access-error [_ _]
  (unauthorized {:error "unauthorized"}))

(defn wrap-restricted [handler rule]
  (restrict handler {:handler  rule
                     :on-error access-error}))

(defmethod restructure-param :auth-rules
  [_ rule acc]
  (update-in acc [:middleware] conj [wrap-restricted rule]))

(defmethod restructure-param :current-user
  [_ binding acc]
  (update-in acc [:letks] into [binding `(:identity ~'+compojure-api-request+)]))

(defapi service-routes
  {:swagger {:ui "/swagger-ui"
             :spec "/swagger.json"
             :data {:info {:version "1.0.0"
                           :title "Sand Compass"
                           :description "Prototype of Interactive "}
                    :tags [{:name "Preflight"     :description "Return successful response for all preflight requests"}
                           {:name "Permission"    :description "Add and remove permissions tied to specific users"}
                           {:name "Auth"          :description "Get auth information for a user"}
                           {:name "Refresh-Token" :description "Get and delete refresh-tokens"}
                           {:name "Password"      :description "Request and confirm password resets"}
                           {:name "User"          :description "Create, delete and update user details"}]}}} 
  preflight-route
  permission-routes
  auth-routes
  refresh-token-routes
  password-routes
  user-routes)