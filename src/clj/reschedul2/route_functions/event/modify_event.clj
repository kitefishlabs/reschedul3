(ns reschedul2.route-functions.event.modify-event
  (:require [reschedul2.db.core :as db]
            [buddy.hashers :as hashers]
            [ring.util.http-response :as respond]
            [taoensso.timbre :as timbre]))

(defn modify-event
  "Update event info (`:name`)"
  [current-event-info name]
  (let [new-name  (if (empty? name) (str (:name current-event-info)) name)
        new-event-info (db/update-event! (.toString (:_id current-event-info))
                                         new-name)]
    (respond/ok {:_id       (.toString (:_id new-event-info))
                 :name  (:name new-event-info)})))

(defn modify-event-response
  "User is allowed to update attributes for an event if the requester is
   modifying an event associated with their own id or has admin permissions."
  [request id name]
  (let [auth               (get-in request [:identity :permission-level])
        current-event-info (db/get-event-by-id (.toString id))
        owner-userid       (:owner-userid current-event-info)
        admin?             (= auth "admin")
        modifying-own?     (= (.toString owner-userid) (get-in request [:identity :_id]))
        admin-or-own?      (or admin? modifying-own?)
        modify?            (and admin-or-own? (not (empty? current-event-info)))]
    (cond
      (not admin-or-own?)         (respond/unauthorized {:error "Not authorized"})
      (empty? current-event-info) (respond/not-found {:error "Event id does not exist"})
      modify?                     (modify-event current-event-info name))))
