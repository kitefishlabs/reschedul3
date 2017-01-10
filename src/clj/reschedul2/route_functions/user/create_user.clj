(ns reschedul2.route-functions.user.create-user
  (:require [reschedul2.db.core :as db]
            [buddy.hashers :as hashers]
            [ring.util.http-response :as respond]))

(defn create-new-user
  "Create user with `email`, `username`, `password`"
  [email username password]
  (let [hashed-password (hashers/encrypt password)
        new-user        (db/stringify-id (db/insert-registered-user! email username hashed-password))
        permission      (db/insert-permission-for-user! (:_id new-user) "basic")]
    (respond/created {} {:username (str (:username new-user))})))

(defn create-user-response
  "Generate response for user creation"
  [email username password]
  (let [username-query   (db/stringify-id (db/get-registered-user-by-username username))
        email-query      (db/stringify-id (db/get-registered-user-by-email email))
        email-exists?    (not-empty email-query)
        username-exists? (not-empty username-query)]
    (cond
      (and username-exists? email-exists?) (respond/conflict {:error "Username and Email already exist"})
      username-exists?                     (respond/conflict {:error "Username already exists"})
      email-exists?                        (respond/conflict {:error "Email already exists"})
      :else                                (create-new-user email username password))))
