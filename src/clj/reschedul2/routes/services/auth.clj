(ns reschedul2.routes.services.auth
  (:require [reschedul2.db.core :as db]
            [reschedul2.validation :as v]
            [reschedul2.routes.services.users :refer :all]
            [reschedul2.routes.services.common :refer :all]
            ; [memory-hole.routes.services.common :refer [handler]]
            [buddy.hashers :as hashers]
            [mount.core :refer [defstate]]
            [clojure.tools.logging :as log]
            [schema.core :as s]
            [clojure.set :refer [rename-keys]]
            [ring.util.http-response :refer :all]))


(defn authenticate-local [username password]
  (when-let [user (db/get-user-by-username {:username username})]
    (when (hashers/check password (:password user))
      (dissoc user :password))))

(def SearchResponse
  {(s/optional-key :users) [User]
   (s/optional-key :error) s/Str})

(def LoginResponse
  {(s/optional-key :user)  User
   (s/optional-key :error) s/Str})

(def LogoutResponse
  {:result s/Str})

(handler find-users [username]
  (ok
    {:users
     (db/get-user-by-username
       username)}))

(handler register! [user]
  (if-let [errors (v/validate-create-user user)]
    (do
      (log/error "error creating user:" errors)
      (bad-request {:error "invalid user"}))

    (do
      (log/debug "dissoc!" (:password user))
      (db/create-user!
        (-> user
            (dissoc :password-confirm)
            (update-in [:password] hashers/encrypt))))))

(handler update-user! [{:keys [password] :as user}]
  (if-let [errors (v/validate-update-user user)]
    (do
      (log/error "error updating user:" errors)
      (bad-request {:error "invalid user"}))
    (ok
      {:user
       (if password
         (db/update-user!
           (-> user
               (dissoc :password-confirm)
               (update :password hashers/encrypt)))
         (db/update-user! user))})))

(defn local-login [usernm pass]
  (when-let [user (authenticate-local usernm pass)]
    (-> user)))
        ; (merge {:member-of    []
                ; :account-name userid)))

(defn login [username password {:keys [session]}]
  (if-let [user (local-login username password)]
    (let [user (dissoc user :password)]
      (-> {:user user}
          (ok)
          (assoc :session (assoc session :identity user))))
    (do
      (log/info "login failed for" username)
      (unauthorized {:error "The username or password was incorrect."}))))

(handler logout []
  (assoc (ok {:result "ok"}) :session nil))
