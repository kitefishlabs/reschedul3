(ns reschedul2.routes.services.event
  (:require [reschedul2.middleware.cors :refer [cors-mw]]
            [reschedul2.middleware.token-auth :refer [token-auth-mw]]
            [reschedul2.middleware.authenticated :refer [authenticated-mw]]
            [reschedul2.route-functions.event.create-event :refer [create-event-response]]
            [reschedul2.route-functions.event.delete-event :refer [delete-event-response]]
            [reschedul2.route-functions.event.modify-event :refer [modify-event-response]]
            [schema.core :as s]
            [compojure.api.sweet :refer :all]))

(def event-routes
  "Specify routes for Event functions"
  (context "/api/v1/event" []
           :tags ["Event"]

    (POST "/"           {:as request}
           :return      {:name String}
           :middleware  [cors-mw]
           :body-params [name :- String]
           :summary     "Create a new event with provided name."
           (create-event-response request name))

    (DELETE "/:id"          {:as request}
             :path-params   [id :- s/Str] ; was a uuid
             :return        {:message String}
             :header-params [authorization :- String]
             :middleware    [token-auth-mw cors-mw authenticated-mw]
             :summary       "Deletes the specified event. Requires token to have `admin` auth or self ID."
             :description   "Authorization header expects the following format 'Token {token}'"
             (delete-event-response request id))

    (PATCH  "/:id"          {:as request}
             :path-params   [id :- s/Str] ; was a uuid
             :body-params   [{name :- String ""}]
             :header-params [authorization :- String]
             :return        {:_id String :name String}
             :middleware    [token-auth-mw cors-mw authenticated-mw]
             :summary       "Update some or all fields of a specified event. Requires token to have `admin` auth or self ID."
             :description   "Authorization header expects the following format 'Token {token}'"
             (modify-event-response request id name))))
