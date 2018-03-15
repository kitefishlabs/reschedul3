(ns reschedul2.route-functions.event.delete-event
  (:require [reschedul2.db.core :as db]
            [ring.util.http-response :as respond]
            [clojure.tools.logging :as log]))

(defn delete-event
  "Delete an event by ID"
  [id]
  (let [deleted-event (db/delete-event! id)]
    (if (not= 0 deleted-event)
      (respond/ok        {:message (format "Event id %s successfully removed" id)})
      (respond/not-found {:error "event id does not exist"}))))

(defn delete-event-response
  "Generate response for event deletion"
  [request event-id]
  (let [auth           (get-in request [:identity :permission-level])
        admin?         (= auth "admin")
        event          (db/get-event-by-id event-id)
        deleting-own?  (= (:owner-userid event) (get-in request [:identity :_id]))]
    (if (or admin? deleting-own?)
      (delete-event event-id)
      (respond/unauthorized {:error "Not authorized"}))))
