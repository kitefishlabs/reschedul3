(ns reschedul2.db.core
  (:require [clojure.walk :refer :all]
            [clj-time.core :as t]
            [monger.core :as mg]
            [monger.collection :as mc]
            [monger.query :as mq]
            [monger.operators :refer :all]
            [monger.result :refer [acknowledged?]]
            [monger.joda-time]
            [mount.core :refer [defstate]]
            [buddy.hashers :as hashers]
            [reschedul2.config :refer [env]]
            [taoensso.timbre :as timbre]
            [ring.util.http-response :refer [not-found]])
 (:import (org.bson.types ObjectId)
          [com.mongodb BasicDBObject BasicDBList]
          java.util.ArrayList))

; Helpers
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
  (mc/drop db "user_permissions"))

(defn clear-password-reset-keys! []
  (mc/drop db "password_reset_keys"))

(defn clear-venues! []
  (mc/drop db "venues"))

(defn clear-events! []
  (mc/drop db "events"))


(defn update-registered-user-password! [id hashedpass]
  (mc/update db "users" {:_id (mongo-friendly id)} {$set {:password hashedpass}}))

(defn get-password-reset-keys-for-userid [qmap]
  (mc/find-one-as-map db "password_reset_keys" qmap))

(defn get-reset-row-by-reset-key [rkey]
  (mc/find-one-as-map db "password_reset_keys" {:reset_key rkey}))

(defn insert-password-reset-key-with-default-valid-until! [rkey uid]
  (mc/insert db "password_reset_keys" {:reset_key rkey}
                                      :_id uid))

(defn insert-password-reset-key-with-valid-until-date! [rkey uid valid]
  (mc/insert db "password_reset_keys" {:reset_key rkey}
                                      :_id uid
                                      :valid_until valid))

(defn invalidate-reset-key! [rk]
  (mc/update db "users" {:reset_key rk} $set :already_used true))


(defn insert-permission! [perm]
  (mc/insert db "permission" {:permission perm}))



(defn all-registered-users []
 (let [res (mq/with-collection db "users"
            (mq/find {})
            ; (mq/paginate :page 1 :per-page 10)
            (mq/sort {:_id -1}))]
  res))

(defn get-registered-user-by-id [id]
  (mc/find-one-as-map db "users" {:_id (ObjectId. id)}))

(defn get-registered-user-by-username [uname]
  (mc/find-one-as-map db "users" {:username uname}))

(defn get-registered-user-by-email [email]
  (mc/find-one-as-map db "users" {:email email}))

(defn registered-user-with-username? [uname]
  (> (mc/count db "users" {:username {$regex uname $options "i"}})
     0))

(defn registered-user-with-email? [email]
  (> (mc/count db "users" {:email {$regex email $options "i"}})
     0))

(defn get-registered-user-details-by-username [uname]
  (get-registered-user-by-username uname))

(defn get-registered-user-details-by-email [email]
  (get-registered-user-by-email email))



(defn get-registered-user-details-by-refresh-token [rtkn]
  (let [refreshed (mc/find-one-as-map db "users" {:refresh_token rtkn})]
    refreshed))



(defn insert-registered-user!
  [eml uname pass state created-on]
  (mc/insert-and-return db "users" {:_id (ObjectId.)
                                    :email eml
                                    :username uname
                                    :password pass
                                    :state state
                                    :created_on created-on}))


; (defn update-registered-user-permission! [id new-perm]
;   ; (timbre/warn (str "new perms: " new-perm))
;   (mc/update-and-return db "users" {:_id (ObjectId. id)} {$set {:permission-level new-perm}}))

(defn update-registered-user!
  [id new-email new-username new-password]
  (let [user-to-refresh (get-registered-user-by-id (.toString id))]
   (mc/save-and-return db "users"
    (merge user-to-refresh
           {:_id (ObjectId. id)
            :email new-email
            :username new-username
            :password new-password}))))


(defn update-registered-user-password!
  [id new-password]
  (mc/update db "users" {:_id (ObjectId. id)} {$set {:password new-password}}))

(defn update-registered-user-state!
  [id new-state]
  (let [user-to-update (get-registered-user-by-id (.toString id))]
   (mc/save-and-return db "users"
    (merge
     user-to-update
     {:state new-state}))))

(defn update-registered-user-refresh-token! [id refresh-token]
  (let [user-to-refresh (get-registered-user-by-id (.toString id))]
    (mc/save-and-return db "users"
      (assoc-in user-to-refresh [:refresh_token] refresh-token))))


(defn nullify-refresh-token! [rtkn]
  (let [user-to-nullify (get-registered-user-details-by-refresh-token rtkn)]
    (if (nil? user-to-nullify)
      nil
      (mc/save-and-return db "users"
        (assoc-in user-to-nullify [:refresh_token] 0)))))


(defn get-permission-for-user [uid]
  (mc/find-one-as-map db "user_permissions" {:user_id (.toString uid)}))

(defn insert-permission-for-user! [uid perm]
  (let [user-perm (get-permission-for-user uid)]
   (if (nil? user-perm)
    (mc/save-and-return db "user_permissions"
     {:_id (ObjectId.)
      :user_id uid
      :permission perm})
    (do
     (mc/remove db "user_permissions"
      {:_id user-perm})
     (mc/save-and-return db "user_permissions"
      {:_id (:_id user-perm)
       :user_id uid
       :permission perm})))))

(defn delete-user-permission! [uid perm]
  (let [uidstr (.toString uid)]
    (do
      (mc/remove db "user_permissions" {:user_id uidstr
                                        :permission perm})
      (insert-permission-for-user! uidstr "basic"))))

(defn delete-registered-user! [id]
  (do
    (mc/remove db "user_permissions" {:user_id (.toString id)
                                      :permission "basic"})
    (mc/remove db "users" {:_id (ObjectId. id)})))



(defn all-venues []
  (mq/with-collection db "venues"
    (mq/find {})
    (mq/fields [:_id])
    (mq/sort {:_id -1})))

(defn get-venue-by-id [id]
  (mc/find-one-as-map db "venues" {:_id (ObjectId. id)}))

(defn get-venue-by-name [vname]
  (mc/find-one-as-map db "venues" {:name vname}))


(defn insert-venue!
  [vname owner-userid created-on]
  (mc/insert-and-return db "venues" {:_id (ObjectId.)
                                     :name vname
                                     :owner_userid owner-userid
                                     :created_on created-on}))
(defn update-venue!
  [id new-name]
  (mc/save-and-return db "venues"
    { :_id (ObjectId. id)
      :name new-name}))

(defn delete-venue! [id]
  (do
    (mc/remove db "venues" {:_id (ObjectId. id)})))

(defn venue-with-name? [vname]
  (> (mc/count db "venues" {:name {$regex vname $options "i"}})
     0))



(defn all-events []
  (mq/with-collection db "events"
    (mq/find {})
    (mq/fields [:_id])
    (mq/sort {:_id -1})))

(defn get-event-by-id [id]
  (mc/find-one-as-map db "events" {:_id (ObjectId. id)}))

(defn get-event-by-name [ename]
  (mc/find-one-as-map db "events" {:name ename}))

(defn insert-event!
  [ename owner-userid created-on]
  (mc/insert-and-return db "events" {:_id (ObjectId.)
                                     :name ename
                                     :owner_userid owner-userid
                                     :created_on created-on}))
(defn update-event!
  [id new-name]
  (mc/save-and-return db "events"
    { :_id (ObjectId. id)
      :name new-name}))

(defn delete-event! [id]
  (do
    (mc/remove db "events" {:_id (ObjectId. id)})))

(defn event-with-name? [ename]
  (> (mc/count db "events" {:name {$regex ename $options "i"}})
     0))


; RESET/seed data

(defn seed-database! []
  ; WARNING : everything stubbed fresh on each reset!
  (clear-password-reset-keys!)
  (clear-users!)
  (clear-user-permissions!)
  (clear-permissions!)
  ; (clear-globals!)
  (clear-venues!)
  (clear-events!))

(defn seed-admin-user! []
 (insert-permission! "basic")
 (insert-permission! "organizer")
 (insert-permission! "admin")
 (insert-registered-user! "tester@kitefishlabs.com" "admin" "youshouldknowbynow99" :verified (t/now))
 (insert-permission-for-user!
  (->
   (get-registered-user-details-by-username "admin")
   :_id
   .toString)
  "admin")
 (= 1 (count (all-registered-users))))

; (seed-database!)
; (seed-admin-user!)
(all-registered-users)

(.toString (:_id (get-registered-user-by-username "Everyman")))
