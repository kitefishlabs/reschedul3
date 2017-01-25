(ns reschedul2.route-functions.refresh-token.gen-new-token
  (:require [reschedul2.general-functions.user.create-token :refer [create-token]]
            [reschedul2.db.core :as db]
            [ring.util.http-response :as respond]
            [taoensso.timbre :as timbre]))

(defn create-new-tokens
  "Create a new user token"
  [user]
  (timbre/warn (str "_ID: " (:_id user) "\n\n\n"))
  (let [new-refresh-token (str (java.util.UUID/randomUUID))
        updated-user      (db/update-registered-user-refresh-token! (:_id user) new-refresh-token)
        perm-level        (db/get-permission-for-user (.toString (:_id updated-user)))]
    (timbre/warn (str "\n\n\n user: " user "\n\n\n")perm-level "\n\n\n")
    {:token (create-token user (:permission perm-level)) :refreshToken new-refresh-token}))

(defn gen-new-token-response
  "Generate response for user token creation"
  [refresh-token]
  (let [user (db/get-registered-user-details-by-refresh-token refresh-token)]
    (if (empty? user)
      (respond/bad-request {:error "Bad Request"})
      (respond/ok          (create-new-tokens user)))))
