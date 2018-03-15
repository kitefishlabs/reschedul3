(ns reschedul2.route-functions.event.create-event
  (:require [reschedul2.db.core :as db]
            [buddy.hashers :as hashers]
            [ring.util.http-response :as respond]
            [taoensso.timbre :as timbre]
            [clj-time.core :as t]
            [clj-time.coerce :as c]))

(defn create-new-event
  "Create event with `name`, `ownerid`"
  [name ownerid]
  (let [new-event        (db/json-friendly (db/insert-event! name ownerid (c/to-long (t/now))))]
    (respond/created {} {:name (str (:name new-event))})))

(defn create-event-response
  "Generate response for event creation"
  [request name]
  (let [owner-id (get-in request [:identity :_id])
        name-exists? (db/event-with-name? name)]

    (cond
      name-exists?                   (respond/conflict {:error "Event with this name already exists"})
      :else                               (create-new-event name owner-id))))
