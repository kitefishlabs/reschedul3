(ns reschedul2.db.core
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.query :as mq]
            [monger.operators :refer :all]
            [monger.result :refer [acknowledged?]]
            [mount.core :refer [defstate]]
            [buddy.hashers :as hashers]
            [reschedul2.config :refer [env]]
            ; [environ.core :refer [env]]
            [taoensso.timbre :as timbre]
            [ring.util.http-response :refer [not-found]])
  (:import (org.bson.types ObjectId)))
;
; Helpers
;


(defn json-friendly [doc]
  (let [friendly-id (:_id doc)]
    (assoc doc :_id (.toString friendly-id))))

(defn mongo-friendly [doc]
  (if (string? (:_id doc))
    (assoc doc :_id (ObjectId. (:_id doc)))
    doc))


(defstate db*
  :start (-> env :database-url mg/connect-via-uri)
  :stop (-> db* :conn mg/disconnect))

(defstate db
  :start (:db db*))

;;;;;;;;;;;;;;;;;
; create-password-reset-key-table-if-not-exists! []
; create-permission-table-if-not-exists! []
; create-basic-permission-if-not-exists! []
; create-registered-user-table-if-not-exists! [])
; truncate-all-tables-in-database! [])
; create-user-permission-table-if-not-exists! [])

;;;;;;;;;;;;;;;;;

(defn clear-users! []
  (mc/drop db "users"))

(defn clear-permissions! []
  (mc/drop db "permission"))

(defn clear-user-permissions! []
  (mc/drop db "user_permission"))

(defn clear-password-reset-keys! []
  (mc/drop db "password-reset-key"))



(defn update-registered-user-password! [id hashedpass]
  (mc/update db "users" {:_id (mongo-friendly id)} {$set {:password hashedpass}}))

(defn get-password-reset-keys-for-userid [qmap]
  (mc/find-one-as-map db "password-reset-key" qmap))

(defn get-reset-row-by-reset-key [rkey]
  (mc/find-one-as-map db "password-reset-key" {:reset_key rkey}))

(defn insert-password-reset-key-with-default-valid-until! [rkey uid]
  (mc/insert db "password-reset-key" {:reset_key rkey
                                      :_id uid}))

(defn insert-password-reset-key-with-valid-until-date! [rkey uid valid]
  (mc/insert db "password-reset-key" {:reset_key rkey
                                      :_id uid
                                      :valid_until valid}))

(defn invalidate-reset-key! [rk]
  (mc/update db "users" {:reset_key rk} $set :already_used true))


(defn insert-permission! [perm]
  (mc/insert db "permission" {:permission perm}))



(defn all-registered-users []
  (mq/with-collection db "users"
    (mq/find {})
    (mq/fields [:_id])
    (mq/sort {:_id -1})))
    ;(mq/paginate :page 1 :per-page 10)))
    ;(mq/fields [:_id :email :username])
    ;(sort (array-map :username 1))
    ;(mq/paginate :page 1 :per-page 10)))

(defn get-registered-user-by-id [id]
  (mc/find-one-as-map db "users" {:_id (ObjectId. id)}))

(defn get-registered-user-by-username [uname]
  (mc/find-one-as-map db "users" {:username uname}))

(defn get-registered-user-by-email [email]
  (mc/find-one-as-map db "users" {:email email}))

(defn registered-users-with-username? [uname]
  (> (mc/count db "users" {:username {$regex uname $options "i"}})
     0))

(defn registered-users-with-email? [email]
  (> (mc/count db "users" {:email {$regex email $options "i"}})
     0))

(defn get-registered-user-details-by-username [uname]
  (get-registered-user-by-username uname))

(defn get-registered-user-details-by-email [email]
  (get-registered-user-by-email email))



(defn get-registered-user-details-by-refresh-token [rtkn]
  (let [refreshed (mc/find-one-as-map db "users" {:refresh_token rtkn})]
    (timbre/warn (str "\n\n\n refreshed: " refreshed "\n\n\n"))
    refreshed))



(defn insert-registered-user!
  [eml uname pass created-on]
  (mc/insert-and-return db "users" {:_id (ObjectId.)
                                    :email eml
                                    :username uname
                                    :password pass
                                    :created_on created-on}))

; (defn update-registered-user-permission! [id new-perm]
;   ; (timbre/warn (str "new perms: " new-perm))
;   (mc/update-and-return db "users" {:_id (ObjectId. id)} {$set {:permission-level new-perm}}))

(defn update-registered-user!
  [id new-email new-username new-password new-refresh-token]
  (mc/save-and-return db "users"
    { :_id (ObjectId. id)
      :email new-email
      :username new-username
      :password new-password
      :refresh_token new-refresh-token}))


(defn update-registered-user-password!
  [id new-password]
  (mc/update db "users" {:_id (ObjectId. id)} {$set {:password new-password}}))

(defn update-registered-user-refresh-token! [id refresh-token]
  (timbre/warn (str "update-registered-user-refresh-token: " id))
  (let [user-to-refresh (get-registered-user-by-id (.toString id))]
    (timbre/warn (str "\n\n\n user-to-refresh: " user-to-refresh "\n\n\n\n\n"))
    (timbre/warn (str "\n\n\n user-to-refresh: " (assoc-in user-to-refresh [:refresh_token] refresh-token) "\n\n\n\n\n"))
    (mc/save-and-return db "users"
      (assoc-in user-to-refresh [:refresh_token] refresh-token))))


(defn nullify-refresh-token! [rtkn]
  (let [user-to-nullify (get-registered-user-details-by-refresh-token rtkn)]
    (timbre/warn (str "user-to-nullify" user-to-nullify "\n\n\n"))
    (if (nil? user-to-nullify)
      nil
      (mc/save-and-return db "users"
        (assoc-in user-to-nullify [:refresh_token] 0)))))


(defn get-permission-for-user [uid]
  (mc/find-one-as-map db "user_permission" {:user_id (.toString uid)}))

(defn insert-permission-for-user! [uid perm]
  (let [user-perm (get-permission-for-user uid)]
   (if (nil? user-perm)
    (mc/save-and-return db "user_permission"
     {:_id (ObjectId.)
      :user_id uid
      :permission perm})
    (do
     (mc/remove db "user_permission"
      {:_id user-perm})
     (mc/save-and-return db "user_permission"
      {:_id (:_id user-perm)
       :user_id uid
       :permission perm})))))

(defn delete-user-permission! [uid perm]
  (let [uidstr (.toString uid)]
    (do
      (mc/remove db "user_permission" {:user_id uidstr
                                       :permission perm})
      (insert-permission-for-user! uidstr "basic"))))

(defn delete-registered-user! [id]
  (do
    (mc/remove db "user_permission" {:user_id (.toString id)
                                     :permission "basic"})
    (mc/remove db "users" {:_id (ObjectId. id)})))



(defn seed-database! [db]
  (timbre/info (str "DB" db)))
  ; (timbre/info (str "DB: " db)))

  ; WARNING : everything stubbed fresh on each reset!

  ; (mc/remove db "password-reset-key")
  ; (mc/remove db "users")
  ; (mc/remove db "user_permission")
  ; (mc/remove db "permission"))
