(ns sand-compass.route-functions.user.create-user
  (:require [sand-compass.db.core :as db]
            [buddy.hashers :as hashers]
            [ring.util.http-response :as respond]
            [taoensso.timbre :as timbre]
            [clj-time.core :as t]
            [clj-time.coerce :as c]))

(defn create-new-user
  "Create user with `username`, `email`, `password`, `state`"
  [username email password state]
  (let [hashed-password (hashers/encrypt password)
        new-user        (db/json-friendly (db/insert-registered-user! email username hashed-password state (c/to-long (t/now))))
        _               (db/insert-permission-for-user! (:_id new-user) "basic")]
    (respond/created {} {:username (str (:username new-user))})))

(defn create-user-response
  "Generate response for user creation"
  [username email password state]
  (let [uname-nil? (nil? username)
        email-nil? (nil? email)
        pass-nil? (nil? password)
        username-exists? (db/registered-user-with-username? username)
        email-exists?    (db/registered-user-with-email? email)]
    (cond
      (and username-exists? email-exists?) (respond/conflict {:error "Username and Email already exist"})
      (or uname-nil? email-nil? pass-nil?) (respond/conflict {:error "Username, Email, Password must be provided"})
      username-exists?                     (respond/conflict {:error "Username already exists"})
      email-exists?                        (respond/conflict {:error "Email already exists"})
      :else                                (create-new-user username email password state))))
