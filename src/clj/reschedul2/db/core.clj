(ns reschedul2.db.core
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.query :as mq]
            [monger.operators :refer :all]
            [monger.result :refer [acknowledged?]]
            [mount.core :refer [defstate]]
            [buddy.hashers :as hashers]
            [reschedul2.config :refer [env]]
            [reschedul2.db.seed :refer [seed-admin-user]]
            [taoensso.timbre :as timbre]
            [ring.util.http-response :refer [not-found]])
  (:import (org.bson.types ObjectId)))
;
; Helpers
;

(defn stringify-id [res]
  (assoc res :_id (str (:_id res))))

(defn objectify-id [res]
  (assoc res :_id (ObjectId. (:_id res))))

(defn stringify-ids [res]
  (map (fn [item]
        (assoc item :_id (str (:_id item)))) (seq res)))

(defn objectify-ids [res]
  (map (fn [item]
        (assoc item :_id (ObjectId. (:_id item))) (seq res))))


(defstate db*
  :start (->
            env
            :database-url
            mg/connect-via-uri)
  :stop (-> db* :conn mg/disconnect))

(defstate db
  :start (:db db*))


;;;;;;;;;;;;;;;;;

; (defn create-password-reset-key-table-if-not-exists! []
;   "password-reset-key")

; (defn create-permission-table-if-not-exists! []
;   "permission-table")

(defn create-basic-permission-if-not-exists! []
  (mc/insert db "permission" {:permission "basic"})) ; UPSERT ??

; (defn create-registered-user-table-if-not-exists! [])

; (defn truncate-all-tables-in-database! [])

; (defn create-user-permission-table-if-not-exists! [])



;;;;;;;;;;;;;;;;;

(defn update-registered-user-password! [userid hashedpass]
  (mc/update db "users" {:_id (ObjectId. userid)} {:password hashedpass}))

; (defn get-password-reset-keys-for-userid [qmap]
;   (mc/find-one-as-map db "password-reset-key" qmap))

(defn get-reset-row-by-reset-key [rkey]
  (mc/find-one-as-map db "password-reset-key" {:reset_key rkey}))

(defn insert-password-reset-key-with-default-valid-until! [rkey uid]
  (mc/insert db "password-reset-key" {:reset_key rkey :user_id uid}))

(defn insert-password-reset-key-with-valid-until-date! [rkey uid valid]
  (mc/insert db "password-reset-key" {:reset_key rkey
                                      :user_id uid
                                      :valid_until valid}))

(defn invalidate-reset-key! [rk]
  (mc/update db "users" {:reset_key rk} $set :already_used true))

(defn insert-permission! [perm]
  (mc/insert db "permission" {:permission perm}))

(defn all-registered-users []
  (mc/find db "users" {}))

(defn get-registered-user-by-id [id]
  (mc/find-one-as-map db "users" {:_id (ObjectId. id)}))

(defn get-registered-user-by-username [uname]
  (mc/find-one-as-map db "users" {:username uname}))

(defn get-registered-user-by-email [eml]
  (mc/find-one-as-map db "users" {:email eml}))


(defn get-registered-user-details-by-username [uname]
  (get-registered-user-by-username uname))

(defn get-registered-user-details-by-email [email]
  (get-registered-user-by-email email))

(defn get-registered-user-details-by-refresh-token [rtkn]
  (mc/find-one-as-map db "users" {:refresh_token rtkn}))



(defn insert-registered-user! [eml uname pass]
  (mc/insert-and-return db "users" {:email eml
                                    :username uname
                                    :password pass}))


(defn update-registered-user! [id new-email new-username new-password new-refresh-token]
  (mc/update db "users" {:_id (ObjectId. id)} {:email new-email
                                               :username new-username
                                               :password new-password
                                               :refresh-token new-refresh-token}))

(defn update-registered-user-password! [id new-password]
  (mc/update db "users" {:_id (ObjectId. id)} {:password new-password}))

(defn update-registered-user-refresh-token! [id refresh-token]
  (mc/update db "users" {:_id (ObjectId. id)} {:refresh-token refresh-token}))

(defn null-refresh-token! [rtkn]
  (mc/update db "users" {:refresh-token rtkn} {:refresh-token "0"}))

(defn delete-registered-user! [id]
  (mc/remove db "users" {:_id (ObjectId. id)}))

(defn insert-permission-for-user! [uid perm]
  (mc/insert-and-return db "user_permission" {:user_id uid :permission perm}))

(defn delete-user-permission! [uid perm]
  (mc/remove db "user_permission" {:user_id uid :permission perm}))

(defn get-permissions-for-userid [uid]
  (mc/find-one-as-map db "user_permission" {:user_id uid}))









; (defn insert-auth-token [auth-record]
;   (mc/insert-and-return db "auth-tokens" auth-record))
;


;
; ; USERS
; (defn create-user! [user]
;   (mc/insert-and-return db "users" (merge user {:_id (ObjectId.)})))
;
; ; http://stackoverflow.com/questions/2342579/http-status-code-for-update-and-delete#2342589
; ; when updating password, does confirm- need to be dissoc'ed???
;
; (defn update-user! [updated-user]
;   (let
;     [_id (ObjectId. (:_id updated-user))
;      res (acknowledged? (mc/update-by-id db "users" _id (dissoc updated-user :_id)))]
;     (stringify-id (mc/find-one-as-map db "users" {:_id _id}))))
;
;
; (defn delete-user! [user]
;   (let [_id (:_id user)]
;     (acknowledged? (mc/remove-by-id db "users" _id))))
;     ; if-let -> ID not found error
;
;
; (defn get-user [id]
;   (stringify-id
;     (mc/find-one-as-map db "users" {:_id (ObjectId. id)})))
;
; (defn get-all-users []
;   (mq/with-collection
;     db
;     "users"
;     (mq/find {})
;     (mq/sort (array-map :name -1))))
;
; (defn get-user-by-id [id]
;   (mc/find-one-as-map db "users" {:_id (ObjectId. id)}))
;
; (defn get-user-by-username [uname]
;   (mc/find-one-as-map db "users" {:username uname}))
;
; (defn get-user-by-email [email]
;   (mq/with-collection
;     db
;     "users"
;     (mq/find {:contact-info.email email})
;     (mq/sort (array-map :last_name -1))))
;



(defn seed-database! []
  ; (let [data-dir (:seed-directory env)
  ;       directory (clojure.java.io/file data-dir)
  ;       files (file-seq directory)
  ;       seed (load-all-seed-venues files)]

  ; (timbre/info "seed venues to insert: " (count seed))
  (timbre/info "DB: " db)
  ;(println (str "\n\n\n" seed "\n\n\n"))
  ;(println (str "\n\n\n" (count (hash-map seed)) "\n\n\n"))

  ; WARNING : everything stubbed fresh on each reset!

  (mc/remove db "users")
  (mc/remove db "users")
  ; (mc/remove @db "venues")
  ; (mc/remove @db "proposals")

  ; (let [response (mc/insert-batch @db "venues" seed)]
  ;   (timbre/info (str "acknowledged?: " (acknowledged? response))))
  ; (timbre/info "seed venues to insert: " (count seed))))

  (timbre/info "created seed admin user"))
  ; (create-user!
  ;   (->
  ;     seed-admin-user
  ;     (dissoc :password-confirm)
  ;     (update-in [:password] hashers/encrypt))))
  ;     ;do other stuff...
