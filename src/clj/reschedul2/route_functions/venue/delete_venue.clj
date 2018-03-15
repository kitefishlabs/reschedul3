(ns reschedul2.route-functions.venue.delete-venue
  (:require [reschedul2.db.core :as db]
            [ring.util.http-response :as respond]
            [clojure.tools.logging :as log]))

(defn delete-venue
  "Delete a venue by ID"
  [id]
  (let [deleted-venue (db/delete-venue! id)]
    (if (not= 0 deleted-venue)
      (respond/ok        {:message (format "venue id %s successfully removed" id)})
      (respond/not-found {:error "Venue id does not exist"}))))

(defn delete-venue-response
  "Generate response for venue deletion"
  [request venue-id]
  (let [auth            (get-in request [:identity :permission-level])
        admin?          (= auth "admin")
        venue           (db/get-venue-by-id venue-id)
        deleting-own?   (= (:owner-userid venue) (get-in request [:identity :_id]))]
    (if (or admin? deleting-own?)
      (db/delete-venue! venue-id)
      (respond/unauthorized {:error "Not authorized"}))))
