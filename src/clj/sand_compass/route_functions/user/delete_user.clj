(ns sand-compass.route-functions.user.delete-user
  (:require [sand-compass.db.core :as db]
            [ring.util.http-response :as respond]
            [clojure.tools.logging :as log]))

(defn delete-user
  "Delete a user by ID"
  [id]
  (let [deleted-user (db/delete-registered-user! id)]
    (log/warn (str "DELETED USER: " deleted-user))
    (if (not= 0 deleted-user)
      (respond/ok        {:message (format "User id %s successfully removed" id)})
      (respond/not-found {:error "Userid does not exist"}))))

(defn delete-user-response
  "Generate response for user deletion"
  [request id]
  (let [auth           (get-in request [:identity :permission-level])
        admin?          (= auth "admin")
        deleting-self? (= (str id) (get-in request [:identity :_id]))]
    (if (or admin? deleting-self?)
      (delete-user id)
      (respond/unauthorized {:error "Not authorized"}))))
