(ns reschedul2.general-functions.user.create-token
  (:require [environ.core :refer [env]]
            [clj-time.core :as time]
            [buddy.sign.jwt :as jwt]
            [reschedul2.db.core :as db]
            [taoensso.timbre :as timbre])
  (:import (org.bson.types ObjectId)))

(defn create-token
  "Create a signed json web token. The token contents are; username, email, id,
   permissions and token expiration time. Tokens are valid for 15 minutes."
  [user perm-level]
  (timbre/warn (str "\n\n\n USER: " user "\n\n\n"))
  (let [stringify-user (->
                         user
                         (update-in [:_id] str)
                         (update-in [:username] str)
                         (update-in [:email] str)
                         (assoc     :permission-level perm-level)
                         (assoc     :exp (time/plus (time/now) (time/seconds 900))))

        token-contents (select-keys stringify-user [:permission-level :username :email :_id :exp])
        _ (timbre/warn (str "\n\n\ntoken-contents: " token-contents "\n\n\n"))
        token-contents-id-str token-contents]
    (jwt/sign token-contents-id-str (env :auth-key) {:alg :hs512})))
