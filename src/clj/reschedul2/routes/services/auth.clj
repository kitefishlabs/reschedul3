(ns reschedul2.routes.services.auth
  (:require [reschedul2.middleware.basic-auth :refer [basic-auth-mw]]
            [reschedul2.middleware.authenticated :refer [authenticated-mw]]
            [reschedul2.middleware.cors :refer [cors-mw]]
            [reschedul2.route-functions.auth.get-auth-credentials :refer [auth-credentials-response]]
            [schema.core :as s]
            [compojure.api.sweet :refer :all]))

(def auth-routes
  "Specify routes for Authentication functions"
  (context "/api/v1/auth" []

     (GET "/"            {:as request}
           :tags          ["Auth"]
           :return        { :_id String
                            :username String
                            :permissions String
                            :token String
                            :refreshToken String}
           :header-params [authorization :- String]
           :middleware    [basic-auth-mw cors-mw authenticated-mw]
           :summary       "Returns auth info given a username and password in the '`Authorization`' header."
           :description   "Authorization header expects '`Basic username:password`' where `username:password`
                           is base64 encoded. To adhere to basic auth standards we have to use a field called
                           `username` however we will accept a valid username or email as a value for this key."
           (auth-credentials-response request))))
