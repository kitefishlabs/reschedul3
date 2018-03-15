(ns reschedul2.route-functions.venue.create-venue
  (:require [reschedul2.db.core :as db]
            [buddy.hashers :as hashers]
            [ring.util.http-response :as respond]
            [taoensso.timbre :as timbre]
            [clj-time.core :as t]
            [clj-time.coerce :as c]))

(defn create-new-venue
  "Create venue with `name`"
  [name ownerid]
  (let [new-venue (db/json-friendly (db/insert-venue! name ownerid (c/to-long (t/now))))]
    (respond/created {} {:name (str (:name new-venue))})))

(defn create-venue-response
  "Generate response for venue creation"
  [request name]
  (let [owner-id (get-in request [:identity :_id])
        name-exists? (db/venue-with-name? name)]
    (cond
      name-exists?                   (respond/conflict {:error "Venue with this name already exists"})
      :else                               (create-new-venue name owner-id))))
