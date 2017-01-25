(ns reschedul2.route-functions.auth.get-auth-credentials
  (:require [reschedul2.general-functions.user.create-token :refer [create-token]]
            [reschedul2.db.core :as db]
            [ring.util.http-response :as respond]
            [taoensso.timbre :as timbre])
  (:import (org.bson.types ObjectId)))

(defn auth-credentials-response
  "Generate response for get requests to /api/v1/auth. This route requires basic
   authentication. A successful request to this route will generate a new
   refresh-token, and return {:_id :username :permission-level :token :refreshToken}"
  [request]
  ; (timbre/warn (str "\n\n\n request: " request "\n\n\n"))
  (let [user          (:identity request)
        refresh-token (str (java.util.UUID/randomUUID))
        ; _             (timbre/warn (str "refresh-token: " refresh-token "\n\n\n"))
        updated-user  (db/update-registered-user-refresh-token! (:_id user) refresh-token)
        perm-level    (db/get-permission-for-user (:_id user))]
        ; TODO: we really should be checking updated-user here + perm level
    ; (timbre/warn (str "updated-user: " updated-user "\n\n\n" perm-level "\n\n\n"))
    (respond/ok {:_id               (.toString (:_id updated-user))
                 :username          (:username updated-user)
                 :permission-level  (:permission perm-level)
                 :token             (create-token user (:permission perm-level))
                 :refreshToken      refresh-token})))
