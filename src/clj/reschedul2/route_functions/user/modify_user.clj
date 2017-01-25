(ns reschedul2.route-functions.user.modify-user
  (:require [reschedul2.db.core :as db]
            [buddy.hashers :as hashers]
            [ring.util.http-response :as respond]))

(defn modify-user
  "Update user info (`:email`/`:username`/`:password`)"
  [current-user-info username password email]
  (let [new-email     (if (empty? email)    (str (:email current-user-info)) email)
        new-username  (if (empty? username) (str (:username current-user-info)) username)
        new-password  (if (empty? password) (:password current-user-info) (hashers/encrypt password))
        new-user-info (db/json-friendly (db/update-registered-user! (:_id current-user-info)
                                                  new-email
                                                  new-username
                                                  new-password
                                                  (:refresh_token current-user-info)))]
    (respond/ok {:_id (:_id current-user-info) :email new-email :username new-username})))

(defn modify-user-response
  "User is allowed to update attributes for a user if the requester is
   modifying attributes associated with its own id or has admin permissions."
  [request id username password email]
  (let [auth              (get-in request [:identity :permission-level])
        current-user-info (db/json-friendly (db/get-registered-user-by-id {:_id id}))
        admin?            (.contains auth "admin")
        modifying-self?   (= (str id) (get-in request [:identity :_id]))
        admin-or-self?    (or admin? modifying-self?)
        modify?           (and admin-or-self? (not-empty current-user-info))]
    (cond
      modify?                    (modify-user current-user-info username password email)
      (not admin?)               (respond/unauthorized {:error "Not authorized"})
      (empty? current-user-info) (respond/not-found {:error "Userid does not exist"}))))
