(ns reschedul2.route-functions.user.modify-user
  (:require [reschedul2.db.core :as db]
            [buddy.hashers :as hashers]
            [ring.util.http-response :as respond]
            [taoensso.timbre :as timbre]))

(defn modify-user
  "Update user info (`:email`/`:username`/`:password`)"
  [current-user-info username password email]
  (let [new-email     (if (empty? email)    (str (:email current-user-info)) email)
        new-username  (if (empty? username) (str (:username current-user-info)) username)
        new-password  (if (empty? password) (:password current-user-info) (hashers/encrypt password))
        new-user-info (db/update-registered-user! (.toString (:_id current-user-info))
                                                  new-email
                                                  new-username
                                                  new-password
                                                  (:refresh_token current-user-info))]
    (timbre/warn "NEW PASS: " new-password)
    (respond/ok {:_id       (.toString (:_id new-user-info))
                 :email     (:email new-user-info)
                 :username  (:username new-user-info)})))

(defn modify-user-response
  "User is allowed to update attributes for a user if the requester is
   modifying attributes associated with its own id or has admin permissions."
  [request id username password email]
  (timbre/warn (str ":identity: " (:identity request) "  " (.toString id)))
  (let [auth              (get-in request [:identity :permission-level])
        current-user-info (db/get-registered-user-by-id (.toString id))
        admin?            (= auth "admin")
        modifying-self?   (= (.toString id) (get-in request [:identity :_id]))
        admin-or-self?    (or admin? modifying-self?)
        modify?           (and admin-or-self? (not (empty? current-user-info)))]
    ; (timbre/warn (str auth " " admin? " " modifying-self? " " admin-or-self? " " modify? " " current-user-info " " current-user-info " " email))
    (timbre/warn (str modify? " " current-user-info "  " username "  " password "  " email))
    (cond
      (not admin-or-self?)       (respond/unauthorized {:error "Not authorized"})
      (empty? current-user-info) (respond/not-found {:error "Userid does not exist"})
      modify?                    (modify-user current-user-info username password email))))
