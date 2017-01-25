(ns reschedul2.routes.services.user
  (:require [reschedul2.middleware.cors :refer [cors-mw]]
            [reschedul2.middleware.token-auth :refer [token-auth-mw]]
            [reschedul2.middleware.authenticated :refer [authenticated-mw]]
            [reschedul2.route-functions.user.create-user :refer [create-user-response]]
            [reschedul2.route-functions.user.delete-user :refer [delete-user-response]]
            [reschedul2.route-functions.user.modify-user :refer [modify-user-response]]
            [schema.core :as s]
            [compojure.api.sweet :refer :all]))


(def user-routes
  "Specify routes for User functions"
  (context "/api/v1/user" []
           :tags ["User"]

    (POST "/"           {:as request}
           :return      {:username String}
           :middleware  [cors-mw]
           :body-params [email :- String username :- String password :- String]
           :summary     "Create a new user with provided username, email and password."
           (create-user-response email username password))

    (DELETE "/:id"          {:as request}
             :path-params   [id :- s/Str] ; was a uuid
             :return        {:message String}
             :header-params [authorization :- String]
             :middleware    [token-auth-mw cors-mw authenticated-mw]
             :summary       "Deletes the specified user. Requires token to have `admin` auth or self ID."
             :description   "Authorization header expects the following format 'Token {token}'"
             (delete-user-response request id))

    (PATCH  "/:id"          {:as request}
             :path-params   [id :- s/Str] ; was a uuid
             :body-params   [{username :- String ""} {password :- String ""} {email :- String ""}]
             :header-params [authorization :- String]
             :return        {:_id String :email String :username String}
             :middleware    [token-auth-mw cors-mw authenticated-mw]
             :summary       "Update some or all fields of a specified user. Requires token to have `admin` auth or self ID."
             :description   "Authorization header expects the following format 'Token {token}'"
             (modify-user-response request id username password email))))
