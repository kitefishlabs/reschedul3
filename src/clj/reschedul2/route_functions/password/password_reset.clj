(ns reschedul2.route-functions.password.password-reset
  (:require [reschedul2.db.core :as db]
            [buddy.hashers :as hashers]
            [clj-time.coerce :as c]
            [clj-time.core :as t]
            [ring.util.http-response :as respond]))


(defn update-password
  "Update user's password"
  [reset-key key-record new-password]
  (let [user-id         (:_id key-record)
        hashed-password (hashers/encrypt new-password)]
    (db/invalidate-reset-key! reset-key)
    (db/update-registered-user-password! user-id hashed-password)
    (respond/ok {:message "Password successfully reset"})))

(defn password-reset-response
  "Generate response for password update"
  [reset-key new-password]
  (let [key-record       (db/json-friendly (db/get-reset-row-by-reset-key reset-key))
        key-exists?      (empty? key-record)
        key-valid-until  (c/from-sql-time (:valid_until key-record))
        key-valid?       (t/before? (t/now) key-valid-until)]
    (cond
      key-exists?                 (respond/not-found {:error "Reset key does not exist"})
      (:already_used key-record)  (respond/not-found {:error "Reset key already used"})
      key-valid?                  (update-password reset-key key-record new-password)
      :else                       (respond/not-found {:error "Reset key has expired"}))))
