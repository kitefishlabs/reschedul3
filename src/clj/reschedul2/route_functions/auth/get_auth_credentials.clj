(ns reschedul2.route-functions.auth.get-auth-credentials
  (:require [reschedul2.general-functions.user.create-token :refer [create-token]]
            [reschedul2.db.core :as db]
            [ring.util.http-response :as respond]
            [taoensso.timbre :as timbre])
  (:import (org.bson.types ObjectId)))

(defn auth-credentials-response
  "Generate response for get requests to /api/v1/auth. This route requires basic
   authentication. A successful request to this route will generate a new
   refresh-token, and return {:_id :username :permission-level :token :refresh_token}"
  [request]
  (let [user          (:identity request)
        refresh-token (str (java.util.UUID/randomUUID))
        updated-user  (db/update-registered-user-refresh-token! (:_id user) refresh-token)
        perm-level    (db/get-permission-for-user (:_id user))]
    (respond/ok {:_id               (.toString (:_id updated-user))
                 :username          (:username updated-user)
                 :permission-level  (:permission perm-level)
                 :token             (create-token user (:permission perm-level))
                 :refresh_token      refresh-token})))
