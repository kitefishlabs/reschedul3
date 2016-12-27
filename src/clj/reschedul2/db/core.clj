(ns reschedul2.db.core
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.query :as mq]
            [monger.operators :refer :all]
            [monger.result :refer [acknowledged?]]
            [mount.core :refer [defstate]]
            [reschedul2.config :refer [env]]
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
  :start (-> env :database-url mg/connect-via-uri)
  :stop (-> db* :conn mg/disconnect))

(defstate db
  :start (:db db*))

(defn create-user! [user]
  (mc/insert-and-return db "users" (merge user {:_id (ObjectId.)})))

; http://stackoverflow.com/questions/2342579/http-status-code-for-update-and-delete#2342589

(defn update-user! [id updated]
  (let
    [_id (ObjectId. id)
     res (acknowledged? (mc/update-by-id db "users" _id (dissoc updated :_id)))]
    (stringify-id (mc/find-one-as-map db "users" {:_id _id}))))
      ;{$set updated}))

(defn delete-user! [_id]
  (acknowledged? (mc/remove-by-id db "users" _id)))


(defn get-user [id]
  (stringify-id
    (mc/find-one-as-map db "users" {:_id (ObjectId. id)})))

(defn get-all-users []
  (mq/with-collection
    db
    "users"
    (mq/find {})
    (mq/sort (array-map :name -1))))

(defn get-user-by-id [id]
  (mc/find-one-as-map db "users" {:_id (ObjectId. id)}))

(defn get-user-by-username [uname]
  (let [res (mc/find-one-as-map db "users" {:username uname})]
    res))

(defn get-user-by-email [email]
  (mq/with-collection
    db
    "users"
    (mq/find {:contact-info.email email})
    (mq/sort (array-map :last_name -1))))
