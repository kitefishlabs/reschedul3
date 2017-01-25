(ns reschedul2.routes.service
  (:require [ring.util.http-response :refer [ok not-found unauthorized]]
            [ring.util.http-status :as http-status]
            [compojure.api.sweet :refer :all]
            [compojure.api.meta :refer [restructure-param]]
            [schema.core :as s]
            [taoensso.timbre :as timbre]
            [reschedul2.db.core :as db]
            ; [reschedul2.routes.services.users :refer [User ContactInfo NewUser UpdatedUser]]
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

; (defn admin? [request]
;   (:admin (:identity request)))
;
; (defn access-error [_ _]
;   (unauthorized {:error "unauthorized"}))
;
; (defn wrap-restricted [handler rule]
;   (restrict handler {:handler  rule
;                      :on-error access-error}))
;
; (defmethod restructure-param :auth-rules
;   [_ rule acc]
;   (update-in acc [:middleware] conj [wrap-restricted rule]))
;
; (defmethod restructure-param :current-user
;   [_ binding acc]
;   (update-in acc [:letks] into [binding `(:identity ~'+compojure-api-request+)]))

(def service-routes
  (api
    {:swagger
      {:ui   "/api-docs"
       :spec "/swagger.json"
       :data {:info {:title "reschedul2"
                     :version "0.0.1"}
              :tags [{:name "Preflight"     :description "Return successful response for all preflight requests"}
                     {:name "User"          :description "Create, delete and update user details"}
                     {:name "Permission"    :description "Add and remove permissions tied to specific users"}
                     {:name "Refresh-Token" :description "Get and delete refresh-tokens"}
                     {:name "Auth"          :description "Get auth information for a user"}]}}}               ; {:name "Password"      :description "Request and confirm password resets"}]}}}
    preflight-route
    user-routes
    permission-routes
    refresh-token-routes
    auth-routes
    password-routes))



;
; (defapi
;   service-routes
;
;   {:swagger {:ui "/swagger-ui"
;              :spec "/swagger.json"
;              :data {:info {:version "1.0.0"
;                            :title "Reschedul2 API"
;                            :description "User, auth, admin, proposals, venues..."}
;                     :tags [{:name "api", :description "api"}]}}}
;
;   (POST "/api/login" req
;     :return auth/LoginResponse
;     :body-params [username :- s/Str
;                   password :- s/Str]
;     :summary "user login handler"
;     (auth/login username password req))
;
;   (context "/admin" []
;     :auth-rules admin?
;     :tags ["admin"]
;
;     (GET "/user/:username" []
;       :path-params [username :- s/Str]
;       :return auth/SearchResponse
;       :summary "returns user(s?) with matching username"
;       (auth/find-users username)))
;
;     ; (GET "/users" []
;     ;   :return auth/SearchResponse
;     ;   :summary "returns all users"
;     ;   (db/get-all-users))
;     ;
;     ; (POST "/user")
;     ; (PUT "/user/:id")
;     ; (DELETE "/user/:id"))
;
;   (context "/api" []
;     :auth-rules authenticated?
;     :tags ["private"]
;
;     (POST "/logout" []
;       :return auth/LogoutResponse
;       :summary "remove the user from the session"
;       (auth/logout))))
;
;






          ;
          ; (context "/user/" []
          ;        (resource
          ;         {:tags [:user]
          ;          :get {:summary "get users"
          ;                :description "get all users"
          ;                :responses {http-status/ok {:schema [User]}}
          ;                :handler (fn [_]
          ;                           (ok (db/json-friendlys (db/get-all-users))))}
          ;          :post {:summary "adds a user"
          ;                 :parameters {:body-params NewUser}
          ;                 :responses {http-status/created {:schema User
          ;                                                  :description "the created user"
          ;                                                  :headers {"Location" s/Str}}}
          ;                 :handler (fn [{body :body-params}]
          ;                            (let [new-user (db/create-user! body)]
          ;                              (timbre/warn (str "new-user: " new-user "\nP"))
          ;                              (ok (db/json-friendly new-user))))}}))
          ;
          ; (context "/user/:id" []
          ;        :path-params [id :- s/Str]
          ;        (resource
          ;         {:tags ["user"]
          ;          :get {:x-name ::user
          ;                :summary "gets a user"
          ;                :responses {ok {:schema User}}
          ;                :handler (fn [_]
          ;                           (if-let [user (db/get-user id)]
          ;                             (ok (db/json-friendly user))
          ;                             (http-status/not-found)))}
          ;          :put {:summary "updates a user"
          ;                :parameters {:body-params UpdatedUser}
          ;                :responses {http-status/ok {:schema User}}
          ;                :handler (fn [{body :body-params}]
          ;                           (if (not (empty? (db/get-user id)))
          ;                             (ok (db/update-user! id body))
          ;                             (http-status/not-found)))}
          ;          :delete {:summary "deletes a user"
          ;                   :handler (fn [_]
          ;                              (db/delete-user! id nil)
          ;                              (http-status/no-content))}})))))    ; (GET "/:id"  []













          ;  ;;tags
          ;  (GET "/tags" []
          ;    :return issues/TagsResult
          ;    :summary "list available tags"
          ;    (issues/tags))
           ;
          ;  (POST "/tag" []
          ;    :body-params [tag :- s/Str]
          ;    :return issues/TagResult
          ;    :summary "add a new tag"
          ;    (issues/add-tag! {:tag tag}))))
