(ns reschedul2.route-functions.permission.add-user-permission
  (:require [reschedul2.db.core :as db]
            [clojure.tools.logging :as log]
            [ring.util.http-response :as respond]))

(defn add-user-permission
  "Create user permission"
  [id permission]
  (let [added-permission (try
                           (db/stringify-id (db/insert-permission-for-user! id permission))
                           (catch Exception e 0))]
    (log/warn (str "added-permission: " added-permission))
    (if (not= 0 added-permission)
      (respond/ok        {:message (format "Permission '%s' for user %s successfully added" permission id)})
      (respond/not-found {:error (format "Permission '%s' does not exist" permission)}))))

(defn add-user-permission-response
  "Generate response for permission creation"
  [request id permission]
  (let [auth (get-in request [:identity :permissions])]
    (if (.contains auth "admin")
      (add-user-permission id permission)
      (respond/unauthorized {:error "Not authorized"}))))
