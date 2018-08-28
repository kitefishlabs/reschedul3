(ns sand-compass.route-functions.user.modify-user
  (:require [sand-compass.db.core :as db]
            [buddy.hashers :as hashers]
            [ring.util.http-response :as respond]
            [taoensso.timbre :as timbre]))

(defn modify-user
  "Update user info (`:username`/`:password`/`:email)" ;`/`:state`/`:role`
  [current-user-info username password email]
  (let [new-email     (if (empty? email)    (str (:email current-user-info)) email)
        new-username  (if (empty? username) (str (:username current-user-info)) username)
        new-password  (if (empty? password) (:password current-user-info) (hashers/encrypt password))
        updated-user-info (db/update-registered-user! (.toString (:_id current-user-info))
                                                      new-email
                                                      new-username
                                                      new-password)]
    (respond/ok (db/json-friendly (dissoc updated-user-info :refresh_token)))))

(defn modify-user-state
  "Update user state (`:state`)"
  [id current-state state]
  (let [new-state         (if (empty? state) current-state state)
        updated-user-info (db/update-registered-user-state! id new-state)]
    (respond/ok (db/json-friendly (dissoc updated-user-info :refresh_token)))))

(defn modify-user-response
  "User is allowed to update attributes for a user if the requester is
   modifying attributes associated with its own id or has admin permissions."
  [request id user]
  (if-let [current-user-info (db/get-registered-user-by-id (.toString id))]
   (let [auth              (get-in request [:identity :permission-level])
         admin?            (= auth "admin")
         modifying-self?   (= (.toString id) (get-in request [:identity :_id]))
         admin-or-self?    (or admin? modifying-self?)
         modify?           (and admin-or-self? (not (empty? current-user-info)))]
     (cond
       (not admin-or-self?)       (respond/unauthorized {:error "Not authorized"})
       modify?                    (modify-user current-user-info (:username user) (:password user) (:email user))))
   (respond/not-found {:error "Userid does not exist"})))


(defn modify-user-state-response
  "Only organizers + admins can adjust user states. Only admins can lock/unlock."
  [request id state]
  (if-let [current-user (db/get-registered-user-by-id (.toString id))]
   (let [auth            (get-in request [:identity :permission-level])
         current-state   (:state current-user)
         current-role    (:role current-user)
         admin?          (= auth "admin")
         org?            (= auth "organizer")
         lock-or-unlock? (or (= state "locked") (= current-state "locked"))
         admin-locking?  (and admin? lock-or-unlock?)
         modify?         (and (not admin-locking?) (or admin? org?))]
     (cond
      (and lock-or-unlock?
           (not admin?))     (respond/unauthorized {:error "Only admin can lock or unlock."})
      (not modify?)          (respond/unauthorized {:error "Only org or admin can verify or unverify."})
      modify?                (modify-user-state (.toString id) current-state state)))
   (respond/not-found {:error "User does not exist"})))
