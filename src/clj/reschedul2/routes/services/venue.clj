(ns reschedul2.routes.services.venue
  (:require [reschedul2.middleware.cors :refer [cors-mw]]
            [reschedul2.middleware.token-auth :refer [token-auth-mw]]
            [reschedul2.middleware.authenticated :refer [authenticated-mw]]
            [reschedul2.route-functions.venue.create-venue :refer [create-venue-response]]
            [reschedul2.route-functions.venue.delete-venue :refer [delete-venue-response]]
            [reschedul2.route-functions.venue.modify-venue :refer [modify-venue-response]]
            [schema.core :as s]
            [compojure.api.sweet :refer :all]))


(def venue-routes
  "Specify routes for Venue functions"
  (context "/api/v1/venue" []
           :tags ["Venue"]

    (POST "/"           {:as request}
           :return      {:name String}
           :middleware  [cors-mw]
           :body-params [name :- String]
           :summary     "Create a new venue with provided name."
           (create-venue-response request name))

    (DELETE "/:id"          {:as request}
             :path-params   [id :- s/Str] ; was a uuid
             :return        {:message String}
             :header-params [authorization :- String]
             :middleware    [token-auth-mw cors-mw authenticated-mw]
             :summary       "Deletes the specified venue. Requires token to have `admin` auth or self ID."
             :description   "Authorization header expects the following format 'Token {token}'"
             (delete-venue-response request id))

    (PATCH  "/:id"          {:as request}
             :path-params   [id :- s/Str] ; was a uuid
             :body-params   [{name :- String ""}]
             :header-params [authorization :- String]
             :return        {:_id String :name String}
             :middleware    [token-auth-mw cors-mw authenticated-mw]
             :summary       "Update some or all fields of a specified venue. Requires token to have `admin` auth or self ID."
             :description   "Authorization header expects the following format 'Token {token}'"
             (modify-venue-response request id name))))
