(ns reschedul2.route-functions.permission.delete-user-permission
  (:require [reschedul2.db.core :as db]
            [ring.util.http-response :as respond]
            [clojure.tools.logging :as log]))

(defn delete-user-permission
  "Remove user permission"
  [id permission]
  (let [deleted-permission (db/delete-user-permission! id permission)]
    (log/warn (str "DELETE-PERMISSION: " deleted-permission))
    (if (not= 0 deleted-permission)
      (respond/ok        {:message (format "Permission '%s' for user %s successfully removed" permission id)})
      (respond/not-found {:error (format "User %s does not have %s permission" id)}))))

(defn delete-user-permission-response
  "Generate response for user permission deletion"
  [request id permission]
  (let [auth (get-in request [:identity :permissions])]
    (if (.contains auth "admin")
      (delete-user-permission id permission)
      (respond/unauthorized {:error "Not authorized"}))))
