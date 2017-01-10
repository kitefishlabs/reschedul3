(ns reschedul2.route-functions.auth.get-auth-credentials
  (:require [reschedul2.general-functions.user.create-token :refer [create-token]]
            [reschedul2.db.core :as db]
            [ring.util.http-response :as respond]))

(defn auth-credentials-response
  "Generate response for get requests to /api/v1/auth. This route requires basic
   authentication. A successful request to this route will generate a new
   refresh-token, and return {:_id :username :permissions :token :refreshToken}"
  [request]
  (let [user          (:identity request)
        refresh-token (str (java.util.UUID/randomUUID))
        _             (db/update-registered-user-refresh-token! refresh-token (:_id user))]
    (respond/ok {:_id            (:_id user)
                 :username      (:username user)
                 :permissions   (:permissions user)
                 :token         (create-token user)
                 :refreshToken  refresh-token})))
