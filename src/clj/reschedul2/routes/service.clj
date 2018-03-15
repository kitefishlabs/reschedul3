(ns reschedul2.routes.service
  (:require [ring.util.http-response :refer [ok not-found unauthorized]]
            [ring.util.http-status :as http-status]
            [compojure.api.sweet :refer :all]
            [compojure.api.meta :refer [restructure-param]]
            [schema.core :as s]
            [taoensso.timbre :as timbre]
            [reschedul2.db.core :as db]
            [reschedul2.routes.services.auth :refer :all]
            [reschedul2.routes.services.password :refer :all]
            [reschedul2.routes.services.permission :refer :all]
            [reschedul2.routes.services.preflight :refer :all]
            [reschedul2.routes.services.refresh-token :refer :all]
            [reschedul2.routes.services.user :refer :all]
            [reschedul2.middleware.basic-auth :refer [basic-auth-mw]]
            [reschedul2.middleware.token-auth :refer [token-auth-mw]]
            [reschedul2.middleware.cors :refer [cors-mw]]
            [buddy.auth.accessrules :refer [restrict]]
            [buddy.auth :refer [authenticated?]]))

(def service-routes
  (api
    {:swagger
      {:ui   "/api-docs"
       :spec "/swagger.json"
       :data {:info {:title "reschedul2"
                     :version "0.0.1"}
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
    user-routes))
    ; venue-routes
    ; event-routes))
