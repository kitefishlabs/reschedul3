(ns reschedul2.route-functions.refresh-token.gen-new-token
  (:require [reschedul2.general-functions.user.create-token :refer [create-token]]
            [reschedul2.db.core :as db]
            [ring.util.http-response :as respond]))

(defn create-new-tokens
  "Create a new user token"
  [user]
  (let [new-refresh-token (str (java.util.UUID/randomUUID))
        _ (db/update-registered-user-refresh-token! new-refresh-token (:_id user))]
    {:token (create-token user) :refreshToken new-refresh-token}))

(defn gen-new-token-response
  "Generate response for user token creation"
  [refresh-token]
  (let [user (db/stringify-id (db/get-registered-user-details-by-refresh-token refresh-token))]
    (if (empty? user)
      (respond/bad-request {:error "Bad Request"})
      (respond/ok          (create-new-tokens user)))))
