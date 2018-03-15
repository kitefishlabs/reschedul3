(ns reschedul2.routes.services.user
  (:require [reschedul2.middleware.cors :refer [cors-mw]]
            [reschedul2.middleware.token-auth :refer [token-auth-mw]]
            [reschedul2.middleware.authenticated :refer [authenticated-mw]]
            [reschedul2.route-functions.user.user :refer :all]
            [reschedul2.route-functions.user.create-user :refer [create-user-response]]
            [reschedul2.route-functions.user.delete-user :refer [delete-user-response]]
            [reschedul2.route-functions.user.modify-user :refer [modify-user-response modify-user-state-response]]
            [schema.core :as s]
            [clojure.walk :refer :all]
            [ring.util.http-response :as respond]
            [compojure.api.sweet :refer :all]
            [reschedul2.db.core :as db]))

(def user-routes
  "Specify routes for User functions"
  (context "/api/v1/user" []
           :tags ["User"]

    (GET "/"              {:as request}
           :return        [User]
           :header-params [authorization :- String]
           :middleware    [token-auth-mw cors-mw authenticated-mw]
           :summary       "Get all user ids."
           (respond/ok (->>
                        (db/all-registered-users)
                        (map db/json-friendly)
                        (map #(dissoc % :refresh_token)))))

   (GET "/:id"           {:as request}
          :return        User
          :header-params [authorization :- String]
          :path-params   [id :- s/Str] ; was a uuid
          :middleware    [token-auth-mw cors-mw authenticated-mw]
          :summary       "Get a user's data."
          (respond/ok (dissoc (db/json-friendly (db/get-registered-user-by-id id)) :refresh_token)))

   (POST "/"           {:as request}
          :return      {:username String}
          :middleware  [cors-mw]
          :body-params [username :- String
                        email :- String
                        password :- String
                        state :- String]
          :summary      "Create a new user with provided username, email and password."
          (create-user-response username email password state))

   (DELETE "/:id"          {:as request}
            :path-params   [id :- s/Str]
            :return        {:message String}
            :header-params [authorization :- String]
            :middleware    [token-auth-mw cors-mw authenticated-mw]
            :summary       "Deletes the specified user. Requires token to have `admin` auth or self ID."
            :description   "Authorization header expects the following format 'Token {token}'"
            (delete-user-response request id))

   (PATCH  "/:id"          {:as request}
            :path-params   [id :- s/Str] ; was a uuid
            :body-params   [user :- User]
            :header-params [authorization :- String]
            :return        User
            :middleware    [token-auth-mw cors-mw authenticated-mw]
            :summary       "Update some or all fields of a specified user. Requires token to have `admin` auth or self ID."
            :description   "Authorization header expects the following format 'Token {token}'"
            (modify-user-response request id user))

   (PATCH  "/:id/state"     {:as request}
            :path-params   [id :- s/Str] ; was a uuid
            :body-params   [{state :- String ""}]
            :header-params [authorization :- String]
            :return        User
            :middleware    [token-auth-mw cors-mw authenticated-mw]
            :summary       "Update state of a specified user. Requires token to have `admin` auth or self ID."
            :description   "Authorization header expects the following format 'Token {token}'"
            (modify-user-state-response request id state))))
