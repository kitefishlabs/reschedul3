(ns reschedul2.routes.services
  (:require [ring.util.http-response :refer [ok not-found]]
            [ring.util.http-status :as http-status]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [reschedul2.db.core :as db]
            [taoensso.timbre :as timbre]))


; Non-public contact info
; + all contact info must be protected by auth/perms
(s/defschema ContactInfo {(s/optional-key :cell-phone)               s/Str
                          (s/optional-key :second-phone)             s/Str
                          (s/optional-key :email)                    s/Str
                          (s/optional-key :address)                  s/Str
                          (s/optional-key :preferred_contact_method) (s/enum :cell :email :second-phone)})

; basic user info and embedded contact + social info
(s/defschema User {:_id                         s/Str
                   :username                    s/Str
                   (s/optional-key :password)   s/Str
                   :first_name                  s/Str
                   :last_name                   s/Str
                   :admin                       s/Bool
                   :role                        s/Str
                   :contact-info                ContactInfo})

(s/defschema NewUser (dissoc User :id))
(s/defschema UpdatedUser NewUser)

(defapi service-routes

        {:swagger {:ui "/swagger-ui"
                   :spec "/swagger.json"
                   :data {:info {:version "1.0.0"
                                 :title "Reschedul2 API"
                                 :description "User, auth, admin, proposals, venues..."}
                          :tags [{:name "user", :description "users"}]}}}

        (context "/user/" []
                 (resource
                  {:tags [:user]
                   :get {:summary "get users"
                         :description "get all users"
                         :responses {http-status/ok {:schema [User]}}
                         :handler (fn [_]
                                    (ok (db/stringify-ids (db/get-all-users))))}
                   :post {:summary "adds a user"
                          :parameters {:body-params NewUser}
                          :responses {http-status/created {:schema User
                                                           :description "the created user"
                                                           :headers {"Location" s/Str}}}
                          :handler (fn [{body :body-params}]
                                     (let [new-user (db/create-user! body)]
                                       (timbre/warn (str "new-user: " new-user "\nP"))
                                       (ok (db/stringify-id new-user))))}}))

        (context "/user/:id" []
                 :path-params [id :- s/Str]
                 (resource
                  {:tags ["user"]
                   :get {:x-name ::user
                         :summary "gets a user"
                         :responses {ok {:schema User}}
                         :handler (fn [_]
                                    (if-let [user (db/get-user id)]
                                      (ok (db/stringify-id user))
                                      (http-status/not-found)))}
                   :put {:summary "updates a user"
                         :parameters {:body-params UpdatedUser}
                         :responses {http-status/ok {:schema User}}
                         :handler (fn [{body :body-params}]
                                    (if (not (empty? (db/get-user id)))
                                      (ok (db/update-user! id body))
                                      (http-status/not-found)))}
                   :delete {:summary "deletes a user"
                            :handler (fn [_]
                                       (db/delete-user! id nil)
                                       (http-status/no-content))}}))); (GET "/:id"  []


    ; (context "/user/username/" [])
    ; (context "/user/email/" [])
    ; (context "/user/zip-code/" [])
    ; (context "/user/public-info/" []))
