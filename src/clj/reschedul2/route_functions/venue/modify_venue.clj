(ns reschedul2.route-functions.venue.modify-venue
  (:require [reschedul2.db.core :as db]
            [buddy.hashers :as hashers]
            [ring.util.http-response :as respond]
            [taoensso.timbre :as timbre]))

(defn modify-venue
  "Update venue info (`:name`)"
  [current-venue-info name]
  (let [new-name  (if (empty? name) (str (:name current-venue-info)) name)
        new-venue-info (db/update-venue! (.toString (:_id current-venue-info))
                                         new-name)]
    (respond/ok {:_id       (.toString (:_id new-venue-info))
                 :name  (:name new-venue-info)})))

(defn modify-venue-response
  "User is allowed to update attributes for an venue if the requester is
   modifying a venue associated with their own id or has admin permissions."
  [request venueid name owner-userid]
  (let [auth               (get-in request [:identity :permission-level])
        current-venue-info (db/get-venue-by-id (.toString venueid))
        owner-userid       (:owner-userid current-venue-info)
        admin?             (= auth "admin")
        modifying-own?     (= (.toString owner-userid) (get-in request [:identity :_id]))
        admin-or-own?      (or admin? modifying-own?)
        modify?            (and admin-or-own? (not (empty? current-venue-info)))]
    (cond
      (not admin-or-own?)         (respond/unauthorized {:error "Not authorized"})
      (empty? current-venue-info) (respond/not-found {:error "Venue id does not exist"})
      modify?                     (modify-venue current-venue-info name))))
