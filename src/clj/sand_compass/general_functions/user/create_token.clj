(ns sand-compass.general-functions.user.create-token
  (:require [environ.core :refer [env]]
            [clj-time.core :as t]
            [buddy.sign.jwt :as jwt]
            [sand-compass.db.core :as db]
            [taoensso.timbre :as timbre])
  (:import (org.bson.types ObjectId)))

(defn create-token
  "Create a signed json web token. The token contents are; username, email, id,
   permissions and token expiration time. Tokens are valid for 15 minutes."
  [user perm-level]
  (let [stringify-user (->
                         user
                         (update-in [:_id] str)
                         (update-in [:username] str)
                         (update-in [:email] str)
                         (assoc     :permission-level perm-level)
                         (assoc     :exp (t/plus (t/now) (t/seconds 900))))

        token-contents (select-keys stringify-user [:permission-level :username :email :_id :exp])
        token-contents-id-str token-contents]
    (jwt/sign token-contents-id-str (env :auth-key) {:alg :hs512})))
