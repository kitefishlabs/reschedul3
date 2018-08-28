(ns sand-compass.route-functions.user.user
  (:require [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [sand-compass.db.core :as db]))

(s/defschema UserState
 (s/enum :created :verified :locked))

(s/defschema User
 {:_id                                s/Str
  :username                           s/Str
  :password                           s/Str
  :email                              s/Str
  :created_on                         s/Int
  :state                              UserState
  (s/optional-key :website)           (s/maybe s/Str)
  (s/optional-key :twitter)           (s/maybe s/Str)
  (s/optional-key :facebook)          (s/maybe s/Str)
  (s/optional-key :refresh_token)     (s/maybe s/Str)})

(s/defschema NewUser {:username s/Str})
(s/defschema UpdatedUser User)
